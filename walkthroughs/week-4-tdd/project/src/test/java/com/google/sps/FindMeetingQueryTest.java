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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 *
 */
@RunWith(JUnit4.class)
public final class FindMeetingQueryTest {
	private static final Collection<Event> NO_EVENTS = Collections.emptySet();
	private static final Collection<String> NO_ATTENDEES = Collections.emptySet();

	// Some people that we can use in our tests.
	private static final String PERSON_A = "Person A";
	private static final String PERSON_B = "Person B";

	// All dates are the first day of the year 2020.
	private static final int TIME_0800AM = TimeRange.getTimeInMinutes(8, 0);
	private static final int TIME_0830AM = TimeRange.getTimeInMinutes(8, 30);
	private static final int TIME_0900AM = TimeRange.getTimeInMinutes(9, 0);
	private static final int TIME_0930AM = TimeRange.getTimeInMinutes(9, 30);
	private static final int TIME_1000AM = TimeRange.getTimeInMinutes(10, 0);
	private static final int TIME_1100AM = TimeRange.getTimeInMinutes(11, 00);

	private static final int DURATION_30_MINUTES = 30;
	private static final int DURATION_60_MINUTES = 60;
	private static final int DURATION_90_MINUTES = 90;
	private static final int DURATION_1_HOUR = 60;
	private static final int DURATION_2_HOUR = 120;

	private FindMeetingQuery query;

	@Before
	public void setUp() {
		query = new FindMeetingQuery();
	}

	@Test
	public void optionsForNoAttendees() {
		MeetingRequest request = new MeetingRequest(NO_ATTENDEES, DURATION_1_HOUR);

		Collection<TimeRange> actual = query.query(NO_EVENTS, request);
		Collection<TimeRange> expected = Arrays.asList(TimeRange.WHOLE_DAY);

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void noOptionsForTooLongOfARequest() {
		// The duration should be longer than a day. This means there should be no options.
		int duration = TimeRange.WHOLE_DAY.duration() + 1;
		MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A), duration);

		Collection<TimeRange> actual = query.query(NO_EVENTS, request);
		Collection<TimeRange> expected = Arrays.asList();

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void eventSplitsRestriction() {
		// The event should split the day into two options (before and after the event).
		Collection<Event> events = Arrays.asList(new Event("Event 1",
				TimeRange.fromStartDuration(TIME_0830AM, DURATION_30_MINUTES), Arrays.asList(PERSON_A)));

		MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A), DURATION_30_MINUTES);

		Collection<TimeRange> actual = query.query(events, request);
		Collection<TimeRange> expected =
				Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
						TimeRange.fromStartEnd(TIME_0900AM, TimeRange.END_OF_DAY, true));

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void everyAttendeeIsConsidered() {
		// Have each person have different events. We should see two options because each person has
		// split the restricted times.
		//
		// Events  :       |--A--|     |--B--|
		// Day     : |-----------------------------|
		// Options : |--1--|     |--2--|     |--3--|

		Collection<Event> events = Arrays.asList(
				new Event("Event 1", TimeRange.fromStartDuration(TIME_0800AM, DURATION_30_MINUTES),
						Arrays.asList(PERSON_A)),
				new Event("Event 2", TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES),
						Arrays.asList(PERSON_B)));

		MeetingRequest request =
				new MeetingRequest(Arrays.asList(PERSON_A, PERSON_B), DURATION_30_MINUTES);

		Collection<TimeRange> actual = query.query(events, request);
		Collection<TimeRange> expected =
				Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0800AM, false),
						TimeRange.fromStartEnd(TIME_0830AM, TIME_0900AM, false),
						TimeRange.fromStartEnd(TIME_0930AM, TimeRange.END_OF_DAY, true));

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void overlappingEvents() {
		// Have an event for each person, but have their events overlap. We should only see two options.
		//
		// Events  :       |--A--|
		//                     |--B--|
		// Day     : |---------------------|
		// Options : |--1--|         |--2--|

		Collection<Event> events = Arrays.asList(
				new Event("Event 1", TimeRange.fromStartDuration(TIME_0830AM, DURATION_60_MINUTES),
						Arrays.asList(PERSON_A)),
				new Event("Event 2", TimeRange.fromStartDuration(TIME_0900AM, DURATION_60_MINUTES),
						Arrays.asList(PERSON_B)));

		MeetingRequest request =
				new MeetingRequest(Arrays.asList(PERSON_A, PERSON_B), DURATION_30_MINUTES);

		Collection<TimeRange> actual = query.query(events, request);
		Collection<TimeRange> expected =
				Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
						TimeRange.fromStartEnd(TIME_1000AM, TimeRange.END_OF_DAY, true));

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void nestedEvents() {
		// Have an event for each person, but have one person's event fully contain another's event. We
		// should see two options.
		//
		// Events  :       |----A----|
		//                   |--B--|
		// Day     : |---------------------|
		// Options : |--1--|         |--2--|

		Collection<Event> events = Arrays.asList(
				new Event("Event 1", TimeRange.fromStartDuration(TIME_0830AM, DURATION_90_MINUTES),
						Arrays.asList(PERSON_A)),
				new Event("Event 2", TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES),
						Arrays.asList(PERSON_B)));

		MeetingRequest request =
				new MeetingRequest(Arrays.asList(PERSON_A, PERSON_B), DURATION_30_MINUTES);

		Collection<TimeRange> actual = query.query(events, request);
		Collection<TimeRange> expected =
				Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
						TimeRange.fromStartEnd(TIME_1000AM, TimeRange.END_OF_DAY, true));

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void doubleBookedPeople() {
		// Have one person, but have them registered to attend two events at the same time.
		//
		// Events  :       |----A----|
		//                     |--A--|
		// Day     : |---------------------|
		// Options : |--1--|         |--2--|

		Collection<Event> events = Arrays.asList(
				new Event("Event 1", TimeRange.fromStartDuration(TIME_0830AM, DURATION_60_MINUTES),
						Arrays.asList(PERSON_A)),
				new Event("Event 2", TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES),
						Arrays.asList(PERSON_A)));

		MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A), DURATION_30_MINUTES);

		Collection<TimeRange> actual = query.query(events, request);
		Collection<TimeRange> expected =
				Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
						TimeRange.fromStartEnd(TIME_0930AM, TimeRange.END_OF_DAY, true));

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void justEnoughRoom() {
		// Have one person, but make it so that there is just enough room at one point in the day to
		// have the meeting.
		//
		// Events  : |--A--|     |----A----|
		// Day     : |---------------------|
		// Options :       |-----|

		Collection<Event> events = Arrays.asList(
				new Event("Event 1", TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
						Arrays.asList(PERSON_A)),
				new Event("Event 2", TimeRange.fromStartEnd(TIME_0900AM, TimeRange.END_OF_DAY, true),
						Arrays.asList(PERSON_A)));

		MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A), DURATION_30_MINUTES);

		Collection<TimeRange> actual = query.query(events, request);
		Collection<TimeRange> expected =
				Arrays.asList(TimeRange.fromStartDuration(TIME_0830AM, DURATION_30_MINUTES));

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void ignoresPeopleNotAttending() {
		// Add an event, but make the only attendee someone different from the person looking to book
		// a meeting. This event should not affect the booking.
		Collection<Event> events = Arrays.asList(new Event("Event 1",
				TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES), Arrays.asList(PERSON_A)));
		MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_B), DURATION_30_MINUTES);

		Collection<TimeRange> actual = query.query(events, request);
		Collection<TimeRange> expected = Arrays.asList(TimeRange.WHOLE_DAY);

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void noConflicts() {
		MeetingRequest request =
				new MeetingRequest(Arrays.asList(PERSON_A, PERSON_B), DURATION_30_MINUTES);

		Collection<TimeRange> actual = query.query(NO_EVENTS, request);
		Collection<TimeRange> expected = Arrays.asList(TimeRange.WHOLE_DAY);

		Assert.assertEquals(expected, actual);
	}

	@Test
	@Ignore
	public void testLots() {
//      events=1000 attendees=1000 avg(ms)=12
//      events=1000 attendees=5000 avg(ms)=108
//      events=1000 attendees=10000 avg(ms)=126
//      events=5000 attendees=1000 avg(ms)=51
//      events=5000 attendees=5000 avg(ms)=259
//      events=5000 attendees=10000 avg(ms)=580
//      events=10000 attendees=1000 avg(ms)=88
//      events=10000 attendees=5000 avg(ms)=529
//      events=10000 attendees=10000 avg(ms)=1206

		for (int i = 0; i < 30; i++) {
			// warm up the compiler
			testPerformance(100, 200, 5);
		}

		Collection<String> results = new ArrayList<>();
		for (int meeting : Arrays.asList(1_000, 5_000, 10_000)) {
			for (int attendee : Arrays.asList(1_000, 5_000, 10_000)) {
				int iterations = 3;
				long totalTime = testPerformance(meeting, attendee, iterations);
				long avgTimeMs = (totalTime / iterations) / 1000 / 1000;

				String result = String.format("events=%d attendees=%d avg(ms)=%d",
						meeting, attendee, avgTimeMs);
				System.out.println(result);
				results.add(result);
			}
		}
		results.forEach(System.out::println);

	}

	private long testPerformance(int events, int attendees, int iterations) {
		List<Event> eventList = makeEvents(events, attendees);
		MeetingRequest request = new MeetingRequest(makeAttendees("nope", attendees), 10);

		long totalTime = 0;
		for (int i = 0; i < iterations; i++) {
			long start = System.nanoTime();
			query.query(eventList, request);
			long end = System.nanoTime();
			long time = end - start;
			System.out.printf("events=%d attendees=%d time(ns)=%d%n", events, attendees, time);
			totalTime += time;
		}

		return totalTime;
	}

	private static List<Event> makeEvents(int eventCount, int attendeeCount) {
		List<Event> events = new ArrayList<>(eventCount);
		Collection<String> attendees = makeAttendees(String.valueOf(0), attendeeCount);
		for (int i = 0; i < eventCount; i++) {
			events.add(new Event("Event-" + i, TimeRange.WHOLE_DAY, attendees));
		}
		return events;
	}

	private static Collection<String> makeAttendees(String prefix, int count) {
		Collection<String> attendees = new ArrayList<>(count);
		for (int a = 0; a < count; a++) {
			attendees.add("Attendee-" + prefix + "-" + a);
		}
		return attendees;
	}
}
