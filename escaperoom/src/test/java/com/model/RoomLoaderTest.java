package com.model;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for RoomLoader.
 */
public class RoomLoaderTest {

    /**
     * Write JSON to a temp file and return the path.
     */
    private Path writeTempFile(String content) throws IOException {
        Path tmp = Files.createTempFile("roomloader-test-", ".json");
        Files.write(tmp, content.getBytes());
        tmp.toFile().deleteOnExit();
        return tmp;
    }

    @Test
    public void testLoadRoomsFromFileArray_parsesRoomsAndPuzzlesAndOptionalFields() throws Exception {
        String json =
                "[" +
                "  {" +
                "    \"name\": \"Test Room\"," +
                "    \"description\": \"A room for testing\"," +
                "    \"isSolved\": true," +
                "    \"level\": 5," +
                "    \"puzzles\": [" +
                "      { \"type\": \"math\", \"question\": \"2+2?\", \"answer\": 4, \"id\": 10, \"locked\": true, \"hiddenHint\": \"Think addition\", \"difficulty\": \"HARD\" }," +
                "      { \"type\": \"door\", \"question\": \"Which door?\", \"numDoors\": 3, \"correctDoor\": 2, \"attempts\": 1, \"id\": 11, \"difficulty\": \"MEDIUM\" }," +
                "      { \"type\": \"trivia\", \"question\": \"Capital of France?\", \"answer\": \"Paris\", \"category\": \"geography\", \"id\": 12 }," +
                "      { \"type\": \"riddle\", \"question\": \"What am I?\", \"answer\": \"Echo\", \"id\": 13 }" +
                "    ]" +
                "  }" +
                "]";

        Path p = writeTempFile(json);

        RoomLoader loader = new RoomLoader();
        List<EscapeRoom> rooms = loader.loadRooms(p.toString());

        assertNotNull(rooms);
        assertEquals(1, rooms.size());

        EscapeRoom room = rooms.get(0);
        assertEquals("Test Room", room.getName());
        assertEquals("A room for testing", room.getDescription());
        assertEquals(5, room.getLevel());
        assertTrue(room.isSolved());

        // ensure we parsed 4 puzzles
        assertNotNull(room.getPuzzles());
        assertEquals(4, room.getPuzzles().size());

        // inspect each puzzle using base-class getters and instanceof checks
        Puzzle math = room.getPuzzles().get(0);
        assertEquals("2+2?", math.getQuestion());
        assertEquals(10, math.getId());
        assertTrue(math.isLocked());
        assertEquals("Think addition", math.getHiddenHint());
        assertEquals(Difficulty.HARD, math.getDifficulty());
        assertTrue(math instanceof MathPuzzle);

        Puzzle door = room.getPuzzles().get(1);
        assertEquals("Which door?", door.getQuestion());
        assertEquals(11, door.getId());
        assertEquals(Difficulty.MEDIUM, door.getDifficulty());
        assertTrue(door instanceof DoorPuzzle);

        Puzzle trivia = room.getPuzzles().get(2);
        assertEquals("Capital of France?", trivia.getQuestion());
        assertEquals(12, trivia.getId());
        assertTrue(trivia instanceof TriviaPuzzle);

        Puzzle riddle = room.getPuzzles().get(3);
        assertEquals("What am I?", riddle.getQuestion());
        assertEquals(13, riddle.getId());
        assertTrue(riddle instanceof RiddlePuzzle);
    }

    @Test
    public void testLoadRoomsFromFileObject_acceptsSingleObjectTopLevel() throws Exception {
        String json =
                "{" +
                "  \"name\": \"Single Room\"," +
                "  \"description\": \"Single object top-level\"," +
                "  \"level\": 2," +
                "  \"puzzle\": [" +
                "    { \"type\": \"riddle\", \"question\": \"I speak without a mouth\", \"answer\": \"Echo\", \"id\": 7 }" +
                "  ]" +
                "}";

        Path p = writeTempFile(json);
        RoomLoader loader = new RoomLoader();
        List<EscapeRoom> rooms = loader.loadRooms(p.toString());

        assertNotNull(rooms);
        assertEquals(1, rooms.size());

        EscapeRoom room = rooms.get(0);
        assertEquals("Single Room", room.getName());
        assertEquals("Single object top-level", room.getDescription());
        assertEquals(2, room.getLevel());
        assertNotNull(room.getPuzzles());
        assertEquals(1, room.getPuzzles().size());
        assertEquals(7, room.getPuzzles().get(0).getId());
    }

    @Test
    public void testNonExistentPathThrowsFileNotFoundException() {
        RoomLoader loader = new RoomLoader();
        try {
            loader.loadRooms("this-path-should-not-exist-12345.json");
            fail("Expected FileNotFoundException for missing resource/path");
        } catch (IOException e) {
            // RoomLoader converts a missing resource into FileNotFoundException with prefix "Cannot open"
            assertTrue(e instanceof java.io.FileNotFoundException);
            assertTrue(e.getMessage().contains("Cannot open"));
        }
    }

    @Test
    public void testInvalidTopLevelJsonThrowsIOException() throws Exception {
        // JSON that parses but is not array or object (e.g. a number)
        Path p = writeTempFile("123");

        RoomLoader loader = new RoomLoader();
        try {
            loader.loadRooms(p.toString());
            fail("Expected IOException for invalid top-level JSON type");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("Top-level JSON must be array or object"));
        }
    }
}
