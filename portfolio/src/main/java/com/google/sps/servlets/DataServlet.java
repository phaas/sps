// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.gson.Gson;
import com.google.sps.JsonConfig;
import com.google.sps.services.CommentService;
import com.google.sps.services.InMemoryCommentService;

import java.io.IOException;
import java.util.Objects;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that returns some example content. TODO: modify this file to handle comments data
 */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

	private final Gson gson;
	private final CommentService commentService;

	public DataServlet() {
		gson = JsonConfig.configureGson();
		commentService = new InMemoryCommentService();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String name = Objects.requireNonNull(req.getParameter("name"), "name is required");
		String message = Objects.requireNonNull(req.getParameter("message"), "message is required");

		commentService.addComment(name, message);
		resp.sendRedirect("/");
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("application/json;");
		response.getWriter().write(gson.toJson(commentService.getComments()));
	}
}
