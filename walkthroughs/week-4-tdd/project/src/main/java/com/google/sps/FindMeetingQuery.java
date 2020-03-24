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

package com.google.sps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class FindMeetingQuery {
	public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
		List<TimeRange> relevantEvents = new ArrayList<>();
		for (Event event : events) {
			for (String attendee : event.getAttendees()) {
				if (request.getAttendees().contains(attendee)) {
					relevantEvents.add(event.getWhen());
					break;
				}
			}
		}

		List<TimeRange> openTimes = findAvailableTimes(relevantEvents);
		List<TimeRange> suitableTimes = openTimes.stream()
				.filter(time -> time.duration() >= request.getDuration())
				.collect(Collectors.toList());
		return suitableTimes;
	}

	private List<TimeRange> findAvailableTimes(List<TimeRange> conflicts) {
		conflicts.sort(TimeRange.ORDER_BY_START);
		List<TimeRange> options = new ArrayList<>();

		int now = TimeRange.START_OF_DAY;
		for (TimeRange when : conflicts) {
			if (now < when.start()) {
				options.add(TimeRange.fromStartEnd(now, when.start(), false));
			}
			if (now < when.end()) {
				now = when.end();
			}
		}
		if (now < TimeRange.END_OF_DAY) {
			options.add(TimeRange.fromStartEnd(now, TimeRange.END_OF_DAY, true));
		}
		return options;
	}


	public Collection<TimeRange> queryGolf(Collection<Event> events, MeetingRequest request) {
		List<TimeRange> unavailableTimes = events.stream()
				.filter(e -> !Collections.disjoint(e.getAttendees(), request.getAttendees()))
				.map(Event::getWhen)
				.sorted(TimeRange.ORDER_BY_START)
				.collect(Collectors.toList());

		List<TimeRange> options = new ArrayList<>();
		int now = TimeRange.START_OF_DAY;
		for (TimeRange when : unavailableTimes) {
			if (now < when.start()) {
				options.add(TimeRange.fromStartEnd(now, when.start(), false));
			}
			now = Math.max(now, when.end());
		}
		if (now < TimeRange.END_OF_DAY) {
			options.add(TimeRange.fromStartEnd(now, TimeRange.END_OF_DAY, true));
		}
		return options.stream()
				.filter(time -> time.duration() >= request.getDuration())
				.collect(Collectors.toList());
	}

}
