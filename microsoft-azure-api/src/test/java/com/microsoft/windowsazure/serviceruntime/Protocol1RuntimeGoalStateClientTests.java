/**
 * 
 */
package com.microsoft.windowsazure.serviceruntime;


import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 */
public class Protocol1RuntimeGoalStateClientTests {
	private final List<GoalState> goalStates = new LinkedList<GoalState>();
	
	@Test
	public void addGoalStateChangedListenerAddsListener() {
		Protocol1RuntimeCurrentStateClient currentStateClient = new Protocol1RuntimeCurrentStateClient(null, null);
				
		GoalStateDeserializer goalStateDeserializer = new ChunkedGoalStateDeserializer();
		
		RoleEnvironmentDataDeserializer roleEnvironmentDeserializer = new RoleEnvironmentDataDeserializer() {
			@Override
			public RoleEnvironmentData deserialize(InputStream stream) {
				return null;
			}
		};
		
		InputChannel inputChannel = new MockInputChannel(new String[] {
				"<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
				"<GoalState>" +
				"<Incarnation>1</Incarnation>" +
				"<ExpectedState>Started</ExpectedState>" +
				"<RoleEnvironmentPath>envpath</RoleEnvironmentPath>" +
				"<CurrentStateEndpoint>statepath</CurrentStateEndpoint>" +
				"<Deadline>2011-03-08T03:27:44.0Z</Deadline>" +				
				"</GoalState>",
				"<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
				"<GoalState>" +
				"<Incarnation>2</Incarnation>" +
				"<ExpectedState>Started</ExpectedState>" +
				"<RoleEnvironmentPath>envpath</RoleEnvironmentPath>" +
				"<CurrentStateEndpoint>statepath</CurrentStateEndpoint>" +
				"<Deadline>2011-03-08T03:27:44.0Z</Deadline>" +				
				"</GoalState>"
		});
		
		Protocol1RuntimeGoalStateClient client = new Protocol1RuntimeGoalStateClient(
				currentStateClient,
				goalStateDeserializer,
				roleEnvironmentDeserializer,
				inputChannel);

		client.addGoalStateChangedListener(new GoalStateChangedListener() {
			@Override
			public void goalStateChanged(GoalState newGoalState) {
				goalStates.add(newGoalState);
			}
		});
		
		goalStates.clear();
		
		try {
			client.getCurrentGoalState();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		assertThat(goalStates.size(), is(1));
		assertThat(goalStates.get(0).getIncarnation().intValue(), is(2));
	}
	
	@Test
	public void goalStateClientRestartsThread() {
		Protocol1RuntimeCurrentStateClient currentStateClient = new Protocol1RuntimeCurrentStateClient(null, null);
				
		GoalStateDeserializer goalStateDeserializer = new GoalStateDeserializer() {
			private ChunkedGoalStateDeserializer deserializer = new ChunkedGoalStateDeserializer();
			
			@Override
			public void initialize(InputStream inputStream) {
				deserializer.initialize(inputStream);
			}
			
			@Override
			public GoalState deserialize() {
				GoalState goalState = deserializer.deserialize();
				
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				goalStates.add(goalState);
				
				return goalState;
			}
		};
		
		RoleEnvironmentDataDeserializer roleEnvironmentDeserializer = new RoleEnvironmentDataDeserializer() {
			@Override
			public RoleEnvironmentData deserialize(InputStream stream) {
				return null;
			}
		};
		
		InputChannel inputChannel = new MockInputChannel(new String[] {
				"<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
				"<GoalState>" +
				"<Incarnation>1</Incarnation>" +
				"<ExpectedState>Started</ExpectedState>" +
				"<RoleEnvironmentPath>envpath</RoleEnvironmentPath>" +
				"<CurrentStateEndpoint>statepath</CurrentStateEndpoint>" +
				"<Deadline>2011-03-08T03:27:44.0Z</Deadline>" +				
				"</GoalState>"
		});
		
		Protocol1RuntimeGoalStateClient client = new Protocol1RuntimeGoalStateClient(
				currentStateClient,
				goalStateDeserializer,
				roleEnvironmentDeserializer,
				inputChannel);

		goalStates.clear();
		
		try {
			client.getCurrentGoalState();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		try {
			client.getCurrentGoalState();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
		
		assertThat(goalStates.size(), is(3));
	}
	
	@Test
	public void getRoleEnvironmentDataReturnsDeserializedData() {		
		Protocol1RuntimeCurrentStateClient currentStateClient = new Protocol1RuntimeCurrentStateClient(null, null);
				
		GoalStateDeserializer goalStateDeserializer = new ChunkedGoalStateDeserializer();
		
		final RoleEnvironmentData data = new RoleEnvironmentData(null, null, null, null, null, false);
		
		RoleEnvironmentDataDeserializer roleEnvironmentDeserializer = new RoleEnvironmentDataDeserializer() {
			@Override
			public RoleEnvironmentData deserialize(InputStream stream) {
				return data;
			}
		};
		
		InputChannel inputChannel = new MockInputChannel(new String[] {
				"<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
				"<GoalState>" +
				"<Incarnation>1</Incarnation>" +
				"<ExpectedState>Started</ExpectedState>" +
				"<RoleEnvironmentPath>envpath</RoleEnvironmentPath>" +
				"<CurrentStateEndpoint>statepath</CurrentStateEndpoint>" +
				"<Deadline>2011-03-08T03:27:44.0Z</Deadline>" +				
				"</GoalState>"
		});
		
		Protocol1RuntimeGoalStateClient client = new Protocol1RuntimeGoalStateClient(
				currentStateClient,
				goalStateDeserializer,
				roleEnvironmentDeserializer,
				inputChannel);

		try {
			assertThat(client.getRoleEnvironmentData(), is(data));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void removeGoalStateChangedListenerRemovesListener() {
		Protocol1RuntimeCurrentStateClient currentStateClient = new Protocol1RuntimeCurrentStateClient(null, null);
				
		GoalStateDeserializer goalStateDeserializer = new ChunkedGoalStateDeserializer();
		
		RoleEnvironmentDataDeserializer roleEnvironmentDeserializer = new RoleEnvironmentDataDeserializer() {
			@Override
			public RoleEnvironmentData deserialize(InputStream stream) {
				return null;
			}
		};
		
		InputChannel inputChannel = new MockInputChannel(new String[] {
				"<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
				"<GoalState>" +
				"<Incarnation>1</Incarnation>" +
				"<ExpectedState>Started</ExpectedState>" +
				"<RoleEnvironmentPath>envpath</RoleEnvironmentPath>" +
				"<CurrentStateEndpoint>statepath</CurrentStateEndpoint>" +
				"<Deadline>2011-03-08T03:27:44.0Z</Deadline>" +				
				"</GoalState>",
				"<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
				"<GoalState>" +
				"<Incarnation>2</Incarnation>" +
				"<ExpectedState>Started</ExpectedState>" +
				"<RoleEnvironmentPath>envpath</RoleEnvironmentPath>" +
				"<CurrentStateEndpoint>statepath</CurrentStateEndpoint>" +
				"<Deadline>2011-03-08T03:27:44.0Z</Deadline>" +				
				"</GoalState>"
		});
		
		Protocol1RuntimeGoalStateClient client = new Protocol1RuntimeGoalStateClient(
				currentStateClient,
				goalStateDeserializer,
				roleEnvironmentDeserializer,
				inputChannel);

		GoalStateChangedListener listener = new GoalStateChangedListener() {
			@Override
			public void goalStateChanged(GoalState newGoalState) {
				goalStates.add(newGoalState);
			}
		};
		
		client.addGoalStateChangedListener(listener);
		client.removeGoalStateChangedListener(listener);
		
		goalStates.clear();
		
		try {
			client.getCurrentGoalState();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		assertThat(goalStates.size(), is(0));
	}
}
