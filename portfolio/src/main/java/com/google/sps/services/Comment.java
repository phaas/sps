package com.google.sps.services;

import java.time.ZonedDateTime;

public class Comment {

	private final String name;
	private final String comment;
	private final ZonedDateTime time;

	public Comment(String name, String comment) {
		this(name, comment, java.time.ZonedDateTime.now());
	}

	protected Comment(String name, String comment, ZonedDateTime time) {
		this.name = name;
		this.comment = comment;
		this.time = time;
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
