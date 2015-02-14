package com.github.nyrkovalex.seed.ssh;

import com.github.nyrkovalex.seed.core.Seed;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class ConsoleUserInfoTest extends com.github.nyrkovalex.seed.test.Seed.Test {
    
    @Mock private Seed.Console console;
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
        assertThat(userInfo.promptPassphrase("test"));
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
        assertThat(userInfo.promptPassword("foo"));
    }
    
    @Test
    public void testShouldReturnPasswordProvided() {
        when(console.readSecure("pass: ")).thenReturn("secret2");
        userInfo.promptPassword("pass");
        assertThat(userInfo.getPassword(), is("secret2"));
    }
    
    @Test
    public void testShouldShouldReturnYes() {
        when(console.read("really?: ")).thenReturn("y");
        assertThat(userInfo.promptYesNo("really?"));
    }
    
    @Test
    public void testShouldShouldIgnoreYesCase() {
        when(console.read("really?: ")).thenReturn("Y");
        assertThat(userInfo.promptYesNo("really?"));
    }
    
    @Test
    public void testShouldReturnNo() {
        when(console.read("really?: ")).thenReturn("n");
        assertThat(!userInfo.promptYesNo("really?"));
    }
}