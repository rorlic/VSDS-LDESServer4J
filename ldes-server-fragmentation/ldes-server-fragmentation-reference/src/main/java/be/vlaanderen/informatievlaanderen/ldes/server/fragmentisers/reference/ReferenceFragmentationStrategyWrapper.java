package be.vlaanderen.informatievlaanderen.ldes.server.fragmentisers.reference;

import be.vlaanderen.informatievlaanderen.ldes.server.domain.model.ConfigProperties;
import be.vlaanderen.informatievlaanderen.ldes.server.fragmentation.FragmentationStrategy;
import be.vlaanderen.informatievlaanderen.ldes.server.fragmentation.FragmentationStrategyWrapper;
import be.vlaanderen.informatievlaanderen.ldes.server.fragmentisers.reference.bucketising.ReferenceBucketiser;
import be.vlaanderen.informatievlaanderen.ldes.server.fragmentisers.reference.config.ReferenceConfig;
import be.vlaanderen.informatievlaanderen.ldes.server.fragmentisers.reference.fragmentation.ReferenceBucketCreator;
import be.vlaanderen.informatievlaanderen.ldes.server.fragmentisers.reference.relations.ReferenceFragmentRelationsAttributer;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.context.ApplicationContext;

import static be.vlaanderen.informatievlaanderen.ldes.server.domain.constants.RdfConstants.RDF_SYNTAX_TYPE;

public class ReferenceFragmentationStrategyWrapper implements FragmentationStrategyWrapper {

	public static final String FRAGMENTATION_PATH = "fragmentationPath";
	public static final String DEFAULT_FRAGMENTATION_PATH = RDF_SYNTAX_TYPE.getURI();

	public static final String FRAGMENTATION_KEY = "fragmentationKey";
	public static final String DEFAULT_FRAGMENTATION_KEY = "reference";

	public FragmentationStrategy wrapFragmentationStrategy(ApplicationContext applicationContext,
			FragmentationStrategy fragmentationStrategy, ConfigProperties properties) {
		final var fragmentationPath = properties.getOrDefault(FRAGMENTATION_PATH, DEFAULT_FRAGMENTATION_PATH);
		final var observationRegistry = applicationContext.getBean(ObservationRegistry.class);
		final var referenceConfig = new ReferenceConfig(fragmentationPath);
		final var referenceBucketiser = new ReferenceBucketiser(referenceConfig);
		final var fragmentationKey = properties.getOrDefault(FRAGMENTATION_KEY, DEFAULT_FRAGMENTATION_KEY);
		final var relationsAttributer = new ReferenceFragmentRelationsAttributer(fragmentationPath, fragmentationKey);

		final var referenceBucketCreator = new ReferenceBucketCreator(relationsAttributer, fragmentationKey);
		return new ReferenceFragmentationStrategy(
				fragmentationStrategy,
				referenceBucketiser,
				referenceBucketCreator,
				observationRegistry
		);
	}

}
