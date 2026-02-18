package dev.arozaakk.booklendingapi.common;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;

import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;

public final class RestDocs {

  private RestDocs() {}

  public static RestDocumentationResultHandler prettyDocument(String identifier) {
    return document(
        identifier, preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()));
  }
}
