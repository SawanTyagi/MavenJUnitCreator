package sampleclass;

import sampleclass.ClassForCreatingJUnit;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.junit.Test;
import org.mockito.Mockito;

import org.mockito.Matchers;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import sampleclass.Other1;
import sampleclass.Other2;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
public class ClassForCreatingJUnitTest {

	@InjectMocks
	private ClassForCreatingJUnit ClassForCreatingJUnit;
	@Mock
	private ArrayList arraylist;
	@Mock
	private HashMap hashMap;
	@Mock
	private HashSet hashset;
	@Mock
	private Other1 other1;
	@Mock
	private Other2 other2;

	@Test
	public void method1Test() {
	}

	@Test
	public void method2Test() {
	}

	private void affecterMock(String returnVarString77, Other1 returnVarOther175, Object returnVarObject16,
			Other2 returnVarOther286, String returnVarString20, boolean returnVarboolean64, boolean returnVarboolean90,
			boolean returnVarboolean86, boolean returnVarboolean49) {
		Mockito.when(other2.other2Method1(Matchers.any())).thenReturn(returnVarString77);
		Mockito.when(other2.other2Method2()).thenReturn(returnVarOther175);
		Mockito.when(hashMap.put(Matchers.any(), Matchers.any())).thenReturn(returnVarObject16);
		Mockito.doNothing().when(other1).other1Method4(Matchers.any(), Matchers.any());
		Mockito.doNothing().when(other1).other1Method3();
		Mockito.when(other1.other1Method2()).thenReturn(returnVarOther286);
		Mockito.when(other1.other1Method1(Matchers.any())).thenReturn(returnVarString20);
		Mockito.when(arraylist.add(Matchers.any())).thenReturn(returnVarboolean64);
		Mockito.when(arraylist.add(Matchers.any())).thenReturn(returnVarboolean90);
		Mockito.when(hashset.add(Matchers.any())).thenReturn(returnVarboolean86);
		Mockito.when(hashset.add(Matchers.any())).thenReturn(returnVarboolean49);
	}
}