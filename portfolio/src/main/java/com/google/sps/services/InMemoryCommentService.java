package com.google.sps.services;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class InMemoryCommentService implements CommentService {

	private final ConcurrentLinkedDeque<Comment> comments = new ConcurrentLinkedDeque<>();

	@Override
	public void addComment(String name, String comment) {
		comments.offer(new Comment(name, comment));
	}

	@Override
	public List<Comment> getComments() {
		return new ArrayList<>(comments);
	}
}
