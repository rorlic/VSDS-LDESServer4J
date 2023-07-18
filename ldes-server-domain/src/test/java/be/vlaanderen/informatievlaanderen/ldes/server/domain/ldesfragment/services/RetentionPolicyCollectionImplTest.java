package be.vlaanderen.informatievlaanderen.ldes.server.domain.ldesfragment.services;

import be.vlaanderen.informatievlaanderen.ldes.server.domain.ldes.retentionpolicy.creation.RetentionPolicyFactory;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.view.valueobject.ViewAddedEvent;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.view.valueobject.ViewDeletedEvent;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.view.valueobject.ViewInitializationEvent;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.viewcreation.entities.ViewSpecification;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.viewcreation.valueobjects.ViewName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class RetentionPolicyCollectionImplTest {

	private final RetentionPolicyFactory retentionPolicyFactory = mock(RetentionPolicyFactory.class);
	private final RetentionPolicyCollectionImpl retentionPolicyCollection = new RetentionPolicyCollectionImpl(
			retentionPolicyFactory);

	@Test
	void test_AddingAndDeletingViews() {
		ViewSpecification viewSpecification = new ViewSpecification(new ViewName("collection", "additonalView"),
				List.of(), List.of());

		assertFalse(retentionPolicyCollection.getRetentionPolicyMap().containsKey(viewSpecification.getName()));
		retentionPolicyCollection.handleViewAddedEvent(new ViewAddedEvent(viewSpecification));

		assertTrue(retentionPolicyCollection.getRetentionPolicyMap().containsKey(viewSpecification.getName()));
		retentionPolicyCollection.handleViewDeletedEvent(new ViewDeletedEvent(viewSpecification.getName()));
		assertFalse(retentionPolicyCollection.getRetentionPolicyMap().containsKey(viewSpecification.getName()));
	}

	@Test
	void test_InitializingViews() {
		ViewSpecification viewSpecification = new ViewSpecification(new ViewName("collection", "additonalView"),
				List.of(), List.of());
		assertFalse(retentionPolicyCollection.getRetentionPolicyMap().containsKey(viewSpecification.getName()));

		retentionPolicyCollection.handleViewInitializationEvent(new ViewInitializationEvent(viewSpecification));

		assertTrue(retentionPolicyCollection.getRetentionPolicyMap().containsKey(viewSpecification.getName()));
	}

}