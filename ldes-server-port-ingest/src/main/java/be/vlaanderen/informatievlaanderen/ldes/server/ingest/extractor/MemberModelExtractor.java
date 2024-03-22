package be.vlaanderen.informatievlaanderen.ldes.server.ingest.extractor;

import be.vlaanderen.informatievlaanderen.ldes.server.ingest.valueobjects.MemberModel;
import org.apache.jena.rdf.model.*;

import java.util.ArrayList;
import java.util.List;

public class MemberModelExtractor {
    private final Model model;
    private final List<Resource> namedSubjects;
    private final List<Resource> processedSubjects = new ArrayList<>();

    private MemberModelExtractor(Model model, List<Resource> namedSubjects) {
        this.model = model;
        this.namedSubjects = namedSubjects;
    }

    public static MemberModelExtractor initialize(Model model) {
        List<Resource> namedSubjects = extractAllNodesSubjects(model);
        return new MemberModelExtractor(model, namedSubjects);
    }

    public List<Resource> getNamedSubjects() {
        return namedSubjects;
    }

    public List<MemberModel> extractAllMemberModels() {
        return namedSubjects.stream()
                .map(subject -> new MemberModel(subject.getURI(), extractMemberModel(subject)))
                .toList();
    }

    public Model extractMemberModel(Resource subject) {
        processedSubjects.add(subject);
        Model member = ModelFactory.createDefaultModel();
        model.listStatements().forEach(statement -> {
            if (statementBelongsToSubject(subject, statement)) {
                member.add(statement);
                if (statementContainsProcessableBNode(statement)) {
                    member.add(extractMemberModel(statement.getObject().asResource()));
                }
            }
        });
        return member;
    }

    private static List<Resource> extractAllNodesSubjects(Model model) {
        return model.listSubjects().toList()
                .stream()
                .filter(subject -> !subject.isAnon())
                .toList();
    }

    private boolean statementBelongsToSubject(RDFNode subject, Statement statement) {
        return statement.getSubject().equals(subject);
    }

    private boolean statementContainsProcessableBNode(Statement statement) {
        return !statement.getObject().isLiteral()
                && !processedSubjects.contains(statement.getObject().asResource())
                && !namedSubjects.contains(statement.getObject().asResource());
    }
}
