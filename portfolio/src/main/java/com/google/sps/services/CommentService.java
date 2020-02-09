package com.google.sps.services;

import java.util.List;

public interface CommentService {

	void addComment(String name, String comment);

	List<Comment> getComments();

}
