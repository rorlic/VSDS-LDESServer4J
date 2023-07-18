package be.vlaanderen.informatievlaanderen.ldes.server.infra.mongo.membersequence.service;

import be.vlaanderen.informatievlaanderen.ldes.server.infra.mongo.membersequence.entity.MemberSequenceEntity;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static org.springframework.data.mongodb.core.FindAndModifyOptions.options;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Component
public class LegacySequenceGeneratorService {

	private final MongoOperations mongoOperations;

	public LegacySequenceGeneratorService(MongoOperations mongoOperations) {
		this.mongoOperations = mongoOperations;
	}

	public long generateSequence(String collectionName) {
		MemberSequenceEntity counter = mongoOperations.findAndModify(
				query(where("_id").is(collectionName)),
				new Update().inc("seq", 1),
				options().returnNew(true).upsert(true),
				MemberSequenceEntity.class);
		return !Objects.isNull(counter) ? counter.getSeq() : 1;
	}
}