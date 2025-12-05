package com.example;

import com.model.Difficulty;
import com.model.DataLoader;
import com.model.UserList;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Small app-wide state holder used by the JavaFX UI to share transient choices
 * (currently: chosenDifficulty) between controllers.
 *
 * New: persistChosenDifficultyToModel() writes the chosen difficulty into the currently
 * logged-in user's Progress (best-effort, defensive using reflection) and saves users.
 */
public final class GameState {
    private static final GameState INSTANCE = new GameState();

    // default to ALL so backstory screens still show appropriate copy
    private com.model.Difficulty chosenDifficulty = com.model.Difficulty.ALL;

    private GameState() {}

    public static GameState get() {
        return INSTANCE;
    }

    public com.model.Difficulty getChosenDifficulty() {
        return chosenDifficulty;
    }

    public void setChosenDifficulty(com.model.Difficulty d) {
        if (d != null) this.chosenDifficulty = d;
    }

    /**
     * Persist the chosenDifficulty into the current user's Progress (best-effort).
     * This is defensive so it works whether Progress exposes an enum setter, a String
     * setter, or a direct field.
     */
    public void persistChosenDifficultyToModel() {
        try {
            UserList ul = UserList.getInstance();
            Object currentUser = null;

            // try common getters
            for (String name : new String[]{"getCurrentUser", "getCurrent", "getLoggedInUser"}) {
                try {
                    Method m = ul.getClass().getMethod(name);
                    Object cu = m.invoke(ul);
                    if (cu != null) { currentUser = cu; break; }
                } catch (Throwable ignored) {}
            }

            // fallback: getAllUsers() and pick first
            if (currentUser == null) {
                try {
                    Method ma = ul.getClass().getMethod("getAllUsers");
                    Object listObj = ma.invoke(ul);
                    if (listObj instanceof List) {
                        List<?> l = (List<?>) listObj;
                        if (!l.isEmpty()) currentUser = l.get(0);
                    }
                } catch (Throwable ignored) {}
            }

            if (currentUser == null) return;

            // obtain progress object
            Object prog = null;
            try {
                Method gp = currentUser.getClass().getMethod("getProgress");
                prog = gp.invoke(currentUser);
            } catch (Throwable ignore) {
                try {
                    Field f = currentUser.getClass().getDeclaredField("progress");
                    f.setAccessible(true);
                    prog = f.get(currentUser);
                } catch (Throwable ignore2) {}
            }
            if (prog == null) return;

            boolean applied = false;

            // 1) try setLastDifficulty(Difficulty)
            try {
                Method m = prog.getClass().getMethod("setLastDifficulty", Difficulty.class);
                m.invoke(prog, this.chosenDifficulty);
                applied = true;
            } catch (Throwable ignored) {}

            // 2) try setLastDifficulty(String)
            if (!applied) {
                try {
                    Method m = prog.getClass().getMethod("setLastDifficulty", String.class);
                    m.invoke(prog, this.chosenDifficulty.name());
                    applied = true;
                } catch (Throwable ignored) {}
            }

            // 3) try alternative setters (loosely named)
            if (!applied) {
                for (String nm : new String[]{"setLastDifficultyAsEnum", "setLastDifficultyEnum", "setDifficulty"}) {
                    try {
                        Method m = prog.getClass().getMethod(nm, Difficulty.class);
                        m.invoke(prog, this.chosenDifficulty);
                        applied = true;
                        break;
                    } catch (Throwable ignored) {}
                    try {
                        Method m2 = prog.getClass().getMethod(nm, String.class);
                        m2.invoke(prog, this.chosenDifficulty.name());
                        applied = true;
                        break;
                    } catch (Throwable ignored) {}
                }
            }

            // 4) fallback: set field "lastDifficulty" directly (enum or string)
            if (!applied) {
                try {
                    Field f = prog.getClass().getDeclaredField("lastDifficulty");
                    f.setAccessible(true);
                    Class<?> tClass = f.getType();
                    if (tClass.isEnum()) {
                        @SuppressWarnings("rawtypes")
                        Class enumClass = (Class) tClass;
                        Object enumVal = Enum.valueOf(enumClass, this.chosenDifficulty.name());
                        f.set(prog, enumVal);
                    } else if (tClass == String.class) {
                        f.set(prog, this.chosenDifficulty.name());
                    } else {
                        // try set name string anyway
                        f.set(prog, this.chosenDifficulty.name());
                    }
                    applied = true;
                } catch (Throwable ignored) {}
            }

            // save users if we wrote something (or even if not — harmless)
            try {
                DataLoader.saveUsers(UserList.getInstance().getAllUsers());
            } catch (Throwable e) {
                // best-effort
                e.printStackTrace();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}

