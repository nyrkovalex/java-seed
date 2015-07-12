package com.github.nyrkovalex.seed.ssh;

import com.github.nyrkovalex.seed.Sys;
import com.github.nyrkovalex.seed.test.MockedTest;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConsoleUserInfoTest extends MockedTest {

	@Mock private Sys.Console console;
	@InjectMocks private ConsoleUserInfo userInfo;

	@Test
	public void testShouldPrintMessage() {
		userInfo.showMessage("test");
		verify(console).printf("test");
	}

	@Test
	public void testShouldAskForPassphrase() {
		userInfo.promptPassphrase("passphrase");
		verify(console).readSecure("passphrase: ");
	}

	@Test
	public void testShouldAlwaysReturnTrueWhenAskedForPassphrase() {
		assertThat(userInfo.promptPassphrase("test"), is(true));
	}

	@Test
	public void testShouldReturnPassphraseProvided() {
		when(console.readSecure("test: ")).thenReturn("secret");
		userInfo.promptPassphrase("test");
		assertThat(userInfo.getPassphrase(), is("secret"));
	}

	@Test
	public void testShouldAskForPassword() {
		userInfo.promptPassword("password");
		verify(console).readSecure("password: ");
	}

	@Test
	public void testShouldAlwaysReturnTrueWhenAskedForPassword() {
		assertThat(userInfo.promptPassword("foo"), is(true));
	}

	@Test
	public void testShouldReturnPasswordProvided() {
		when(console.readSecure("pass: ")).thenReturn("secret2");
		userInfo.promptPassword("pass");
		assertThat(userInfo.getPassword(), is("secret2"));
	}

	@Test
	public void testShouldShouldReturnYes() {
		when(console.readLine("really?: ")).thenReturn("y");
		assertThat(userInfo.promptYesNo("really?"), is(true));
	}

	@Test
	public void testShouldShouldIgnoreYesCase() {
		when(console.readLine("really?: ")).thenReturn("Y");
		assertThat(userInfo.promptYesNo("really?"), is(true));
	}

	@Test
	public void testShouldReturnNo() {
		when(console.readLine("really?: ")).thenReturn("n");
		assertThat(userInfo.promptYesNo("really?"), is(false));
	}
}
