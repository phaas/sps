package com.google.sps.services;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class DatastoreCommentService implements CommentService {

	public static final String ENTITY_KIND = "Comment";
	private final DatastoreService datastore;

	public DatastoreCommentService() {
		datastore = DatastoreServiceFactory.getDatastoreService();
	}

	@Override
	public void addComment(String name, String comment) {
		Entity entity = new Entity(ENTITY_KIND);
		entity.setProperty("name", name);
		entity.setProperty("comment", comment);
		entity.setProperty("time", ZonedDateTime.now().toString());
		datastore.put(entity);
	}

	@Override
	public List<Comment> getComments() {
		Query query = new Query("Comment").addSort("time", SortDirection.DESCENDING);

		return datastore.prepare(query)
				.asList(FetchOptions.Builder.withDefaults())
				.stream()
				.map(DatastoreCommentService::convertEntityToComment)
				.collect(Collectors.toList());
	}

	/**
	 * Map a DataStore Entity to a Comment object
	 */
	private static Comment convertEntityToComment(Entity e) {
		return new Comment(
				(String) e.getProperty("name"),
				(String) e.getProperty("comment"),
				ZonedDateTime.parse((String) e.getProperty("time")));
	}
}
