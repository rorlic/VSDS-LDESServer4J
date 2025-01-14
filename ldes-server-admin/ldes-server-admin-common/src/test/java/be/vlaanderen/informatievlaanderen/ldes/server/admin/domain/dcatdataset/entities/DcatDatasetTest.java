package be.vlaanderen.informatievlaanderen.ldes.server.admin.domain.dcatdataset.entities;

import be.vlaanderen.informatievlaanderen.ldes.server.admin.domain.dcat.dcatdataset.entities.DcatDataset;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DcatDatasetTest {

	@Test
	void when_CallingGetDatasetIriString_should_ReturnTheCorrectIriString() {
		String result = new DcatDataset("collectionName").getDatasetIriString("http://localhost.dev");

		Assertions.assertEquals("http://localhost.dev/collectionName", result);
	}

}