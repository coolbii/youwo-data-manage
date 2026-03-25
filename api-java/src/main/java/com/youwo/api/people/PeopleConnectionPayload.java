package com.youwo.api.people;

import java.util.List;

public record PeopleConnectionPayload(
    List<PersonPayload> nodes, PeoplePageInfoPayload pageInfo, long totalCount) {}
