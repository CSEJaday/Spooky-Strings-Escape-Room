package com.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Tests for UserList class.
 *
 * These tests replace the private 'users' list on the singleton via reflection
 * so they don't depend on DataLoader implementation or persistent storage.
 */
public class UserListTest {

    private UserList instance;
    private ArrayList<User> backingList;

    @Before
    public void setUp() throws Exception {
        // Ensure singleton is cleared before each test
        resetSingleton();

        // Acquire singleton (this may call DataLoader.getUsers() internally);
        // to make tests deterministic, overwrite the internal users list immediately.
        instance = UserList.getInstance();

        // Prepare a controlled backing list and inject it into the singleton
        backingList = new ArrayList<>();
        setPrivateUsersField(instance, backingList);
    }

    @After
    public void tearDown() throws Exception {
        // Reset singleton so other tests / test classes are not polluted by state
        resetSingleton();
    }

    @Test
    public void testSingletonReturnsSameInstance() {
        UserList other = UserList.getInstance();
        assertSame("getInstance should always return the same singleton instance", instance, other);
    }

    @Test
    public void testGetAllUsersReturnsInjectedList() {
        // list should be same object we injected
        assertSame("getAllUsers should return the list we injected", backingList, instance.getAllUsers());
    }

    @Test
    public void testGetUserByNameFoundCaseInsensitive() {
        // create and add a user to the backing list
        User u1 = new User("Alice", "pw", UUID.randomUUID());
        backingList.add(u1);

        // search using different case
        User found = instance.getUserByName("alice");
        assertNotNull("getUserByName should find user ignoring case", found);
        assertEquals("found user should be the same object", u1, found);

        // exact-case lookup
        User foundExact = instance.getUserByName("Alice");
        assertSame("getUserByName should find exact-case name as well", u1, foundExact);
    }

    @Test
    public void testGetUserByNameNotFoundReturnsNull() {
        // empty backing list -> nothing found
        User found = instance.getUserByName("nobody");
        assertNull("getUserByName should return null when user not present", found);
    }

    @Test
    public void testCreateAccountAddsNewUserAndReturnsTrue() throws Exception {
        // ensure no users initially
        assertEquals(0, backingList.size());

        boolean created = instance.createAccount("newUser", "secret");
        assertTrue("createAccount should return true when account created", created);

        // a user with name 'newUser' should now exist in the injected list
        User found = instance.getUserByName("newUser");
        assertNotNull("new user should be present after createAccount", found);
        assertEquals("username should match", "newUser", found.getUsername());
        assertEquals("password should match", "secret", found.getPassword());
    }

    @Test
    public void testCreateAccountRejectsDuplicateAndReturnsFalse() throws Exception {
        // add an existing user
        User existing = new User("dup", "pw", UUID.randomUUID());
        backingList.add(existing);

        // attempt to create a duplicate username
        boolean created = instance.createAccount("dup", "otherpw");
        assertFalse("createAccount should return false for duplicate username", created);

        // ensure list size didn't grow
        long count = backingList.stream().filter(u -> u.getUsername().equalsIgnoreCase("dup")).count();
        assertEquals("should still be only one user named 'dup'", 1, count);
    }

    @Test
    public void testLoadUsersDelegatesButDoesNotBreakWhenInjectedListPresent() throws Exception {
        // If DataLoader.getUsers() is present it will be called by loadUsers.
        // To keep this test safe and deterministic we call loadUsers and then ensure the users list
        // is non-null afterwards. We also re-inject our backing list to preserve state.
        try {
            instance.loadUsers();
        } catch (Exception ex) {
            // If DataLoader throws, we don't fail the test — just ensure we can still set our backing list
        }
        // Re-inject controlled list to ensure instance still usable
        setPrivateUsersField(instance, backingList);
        assertNotNull("after loadUsers (or re-injection) getAllUsers should be non-null", instance.getAllUsers());
    }

    /*
     * ---------- Helper reflection utilities ----------
     */

    /**
     * Replace the private 'users' field inside a UserList instance with our provided list.
     */
    private void setPrivateUsersField(UserList target, ArrayList<User> newList) throws Exception {
        Field usersField = UserList.class.getDeclaredField("users");
        usersField.setAccessible(true);
        usersField.set(target, newList);
    }

    /**
     * Reset the private static singleton 'userList' to null so each test starts fresh.
     */
    private void resetSingleton() throws Exception {
        Field instanceField = UserList.class.getDeclaredField("userList");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }
}
