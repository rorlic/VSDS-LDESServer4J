package be.vlaanderen.informatievlaanderen.ldes.server.admin.domain.view.service;

import be.vlaanderen.informatievlaanderen.ldes.server.domain.constants.RdfConstants;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigFilterPredicateTest {

	@Test
	void when_StatementsContainAStatementWithSamePredicateAsConfigFilterPredicate_then_TheseAreExtracted()
			throws URISyntaxException {
		Model viewModel = readModelFromFile("view/view_valid.ttl");
		ConfigFilterPredicate retentionFilter = new ConfigFilterPredicate(RdfConstants.RETENTION_TYPE);
		ConfigFilterPredicate fragmentationFilter = new ConfigFilterPredicate(RdfConstants.FRAGMENTATION_OBJECT);

		assertEquals(1, viewModel.listStatements().toList().stream()
				.filter(retentionFilter)
				.count());
		assertEquals(1, viewModel.listStatements().toList().stream()
				.filter(fragmentationFilter)
				.count());
	}

	private Model readModelFromFile(String fileName) throws URISyntaxException {
		ClassLoader classLoader = getClass().getClassLoader();
		String uri = Objects.requireNonNull(classLoader.getResource(fileName)).toURI()
				.toString();
		return RDFDataMgr.loadModel(uri);
	}

}
