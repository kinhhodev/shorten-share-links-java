package com.shortlink.app.event;

import java.util.UUID;

public record LinkClickedEvent(UUID linkId) {}
