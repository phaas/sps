package com.google.sps.services;

import java.time.ZonedDateTime;

public class Comment {

	private final String name, comment;
	private final ZonedDateTime time;

	public Comment(String name, String comment) {
		this.name = name;
		this.comment = comment;
		this.time = java.time.ZonedDateTime.now();
	}

	public String getName() {
		return name;
	}

	public String getComment() {
		return comment;
	}

	public ZonedDateTime getTime() {
		return time;
	}
}
