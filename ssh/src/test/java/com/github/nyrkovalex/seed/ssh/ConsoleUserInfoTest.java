package com.github.nyrkovalex.seed.ssh;

import com.github.nyrkovalex.seed.Expect;
import com.github.nyrkovalex.seed.Seed;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class ConsoleUserInfoTest extends Expect.Test {

    @Mock private Seed.Console console;
    @InjectMocks private ConsoleUserInfo userInfo;

    @Test
    public void testShouldPrintMessage() {
        userInfo.showMessage("test");
        expect(console).toHaveCall().printf("test");
    }

    @Test
    public void testShouldAskForPassphrase() {
        userInfo.promptPassphrase("passphrase");
        expect(console).toHaveCall().readSecure("passphrase: ");
    }

    @Test
    public void testShouldAlwaysReturnTrueWhenAskedForPassphrase() {
        expect(userInfo.promptPassphrase("test")).toBe(Boolean.TRUE);
    }

    @Test
    public void testShouldReturnPassphraseProvided() {
        given(console.readSecure("test: ")).returns("secret");
        userInfo.promptPassphrase("test");
        expect(userInfo.getPassphrase()).toBe(("secret"));
    }

    @Test
    public void testShouldAskForPassword() {
        userInfo.promptPassword("password");
        expect(console).toHaveCall().readSecure("password: ");
    }

    @Test
    public void testShouldAlwaysReturnTrueWhenAskedForPassword() {
        expect(userInfo.promptPassword("foo")).toBe(Boolean.TRUE);
    }

    @Test
    public void testShouldReturnPasswordProvided() {
        given(console.readSecure("pass: ")).returns("secret2");
        userInfo.promptPassword("pass");
        expect(userInfo.getPassword()).toBe("secret2");
    }

    @Test
    public void testShouldShouldReturnYes() {
        given(console.read("really?: ")).returns("y");
        expect(userInfo.promptYesNo("really?")).toBe(Boolean.TRUE);
    }

    @Test
    public void testShouldShouldIgnoreYesCase() {
        given(console.read("really?: ")).returns("Y");
        expect(userInfo.promptYesNo("really?")).toBe(Boolean.TRUE);
    }

    @Test
    public void testShouldReturnNo() {
        given(console.read("really?: ")).returns("n");
        expect(userInfo.promptYesNo("really?")).toBe(Boolean.FALSE);
    }
}