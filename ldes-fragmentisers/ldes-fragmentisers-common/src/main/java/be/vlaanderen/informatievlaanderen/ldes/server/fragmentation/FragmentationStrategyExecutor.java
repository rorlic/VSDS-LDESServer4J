package be.vlaanderen.informatievlaanderen.ldes.server.fragmentation;

import be.vlaanderen.informatievlaanderen.ldes.server.domain.model.ViewName;
import be.vlaanderen.informatievlaanderen.ldes.server.fragmentation.entities.FragmentSequence;
import be.vlaanderen.informatievlaanderen.ldes.server.fragmentation.entities.Member;
import be.vlaanderen.informatievlaanderen.ldes.server.fragmentation.repository.FragmentSequenceRepository;
import be.vlaanderen.informatievlaanderen.ldes.server.ingest.EventSourceService;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.observation.ObservationRegistry;
import org.apache.jena.rdf.model.Model;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static io.micrometer.observation.Observation.createNotStarted;

public class FragmentationStrategyExecutor {

	private static final String LDES_SERVER_FRAGMENTED_MEMBERS_COUNT = "ldes_server_fragmented_members_count for view: ";

	private final ExecutorService executorService;
	private final FragmentationStrategy fragmentationStrategy;
	private final ViewName viewName;
	private final RootFragmentRetriever rootFragmentRetriever;
	private final ObservationRegistry observationRegistry;
	private final EventSourceService eventSourceService;
	private final FragmentSequenceRepository fragmentSequenceRepository;
	private boolean isExecutorActive = true;

	public FragmentationStrategyExecutor(ViewName viewName,
			FragmentationStrategy fragmentationStrategy,
			RootFragmentRetriever rootFragmentRetriever,
			ObservationRegistry observationRegistry,
			ExecutorService executorService,
			EventSourceService eventSourceService,
			FragmentSequenceRepository fragmentSequenceRepository) {
		this.rootFragmentRetriever = rootFragmentRetriever;
		this.observationRegistry = observationRegistry;
		this.eventSourceService = eventSourceService;
		this.executorService = executorService;
		this.fragmentationStrategy = fragmentationStrategy;
		this.viewName = viewName;
		this.fragmentSequenceRepository = fragmentSequenceRepository;
	}

	public void execute() {
		executorService.execute(addMembersToFragments());
	}

	private Runnable addMembersToFragments() {
		return () -> {
			var nextMemberToFragment = getNextMemberToFragment(determineLastProcessedSequence());

			while (nextMemberToFragment.isPresent() && isExecutorActive) {
				final FragmentSequence lastProcessedSequence = fragment(nextMemberToFragment.get());
				Metrics.counter(LDES_SERVER_FRAGMENTED_MEMBERS_COUNT, "view",  viewName.asString()).increment();
				nextMemberToFragment = getNextMemberToFragment(lastProcessedSequence);
			}
		};
	}

	private FragmentSequence determineLastProcessedSequence() {
		return fragmentSequenceRepository.findLastProcessedSequence(viewName)
				.orElse(FragmentSequence.createNeverProcessedSequence(viewName));
	}

	private Optional<Member> getNextMemberToFragment(FragmentSequence lastProcessedSequence) {
		final String collectionName = viewName.getCollectionName();
		final long lastProcessedSequenceNr = lastProcessedSequence.sequenceNr();
		return eventSourceService
				.findFirstByCollectionNameAndSequenceNrGreaterThan(collectionName, lastProcessedSequenceNr)
				.map(member -> new Member(member.getId(), member.getModel(), member.getSequenceNr()));
	}

	private FragmentSequence fragment(Member member) {
		var parentObservation = createNotStarted("execute fragmentation", observationRegistry).start();
		var rootFragmentOfView = rootFragmentRetriever.retrieveRootFragmentOfView(viewName, parentObservation);
		String memberId = member.id();
		Model memberModel = member.model();
		fragmentationStrategy.addMemberToFragment(rootFragmentOfView, memberId, memberModel, parentObservation);
		final FragmentSequence lastProcessedSequence = new FragmentSequence(viewName, member.sequenceNr());
		fragmentSequenceRepository.saveLastProcessedSequence(lastProcessedSequence);
		parentObservation.stop();
		return lastProcessedSequence;
	}

	public boolean isPartOfCollection(String collectionName) {
		return Objects.equals(viewName.getCollectionName(), collectionName);
	}

	public ViewName getViewName() {
		return viewName;
	}

	public void shutdown() {
		isExecutorActive = false;
		executorService.shutdown();
		try {
			// noinspection ResultOfMethodCallIgnored
			executorService.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		FragmentationStrategyExecutor that = (FragmentationStrategyExecutor) o;
		return Objects.equals(getViewName(), that.getViewName());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getViewName());
	}

}
