package com.bofa.kafkastreampoc.doa;

import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.annotation.JsonInclude;

//	ignore null fields , class level
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class GenericJsonNode extends JsonNode {

}
