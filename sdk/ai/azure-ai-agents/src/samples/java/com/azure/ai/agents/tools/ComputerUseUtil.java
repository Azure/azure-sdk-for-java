// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseComputerToolCall;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Shared helper functions and classes for Computer Use Agent samples.
 */
public final class ComputerUseUtil {

    private ComputerUseUtil() {
        // Utility class
    }

    /**
     * Enum for tracking the state of the simulated web search workflow.
     */
    public enum SearchState {
        /** Browser search page - initial state. */
        INITIAL,
        /** Text entered in search box. */
        TYPED,
        /** Enter key pressed, transitioning to results. */
        PRESSED_ENTER
    }

    /**
     * Screenshot information containing filename and data URL.
     */
    public static class ScreenshotInfo {
        private final String filename;
        private final String url;

        /**
         * Creates a new ScreenshotInfo instance.
         *
         * @param filename The filename of the screenshot.
         * @param url The data URL of the screenshot.
         */
        public ScreenshotInfo(String filename, String url) {
            this.filename = filename;
            this.url = url;
        }

        /**
         * Gets the filename.
         *
         * @return The filename.
         */
        public String getFilename() {
            return filename;
        }

        /**
         * Gets the data URL.
         *
         * @return The data URL.
         */
        public String getUrl() {
            return url;
        }
    }

    /**
     * Result of handling a computer action.
     */
    public static class HandleActionResult {
        private final ScreenshotInfo screenshotInfo;
        private final SearchState state;

        /**
         * Creates a new HandleActionResult instance.
         *
         * @param screenshotInfo The screenshot info to return.
         * @param state The updated search state.
         */
        public HandleActionResult(ScreenshotInfo screenshotInfo, SearchState state) {
            this.screenshotInfo = screenshotInfo;
            this.state = state;
        }

        /**
         * Gets the screenshot info.
         *
         * @return The screenshot info.
         */
        public ScreenshotInfo getScreenshotInfo() {
            return screenshotInfo;
        }

        /**
         * Gets the updated search state.
         *
         * @return The search state.
         */
        public SearchState getState() {
            return state;
        }
    }

    /**
     * Convert an image file to a Base64-encoded string.
     *
     * @param imagePath The path to the image file.
     * @return A Base64-encoded string representing the image.
     * @throws IOException If the file cannot be read.
     */
    public static String imageToBase64(Path imagePath) throws IOException {
        byte[] fileData = Files.readAllBytes(imagePath);
        return Base64.getEncoder().encodeToString(fileData);
    }

    /**
     * Load and convert screenshot images to base64 data URLs.
     *
     * @return Dictionary mapping state names to screenshot info with filename and data URL.
     * @throws IOException If any required screenshot asset files are missing.
     */
    public static Map<String, ScreenshotInfo> loadScreenshotAssets() throws IOException {
        // Load demo screenshot images from assets directory
        // Flow: search page -> typed search -> search results
        Path assetsDir = findAssetsDirectory();

        Map<String, Path> screenshotPaths = new HashMap<>();
        screenshotPaths.put("browser_search", assetsDir.resolve("cua_browser_search.png"));
        screenshotPaths.put("search_typed", assetsDir.resolve("cua_search_typed.png"));
        screenshotPaths.put("search_results", assetsDir.resolve("cua_search_results.png"));

        Map<String, String> filenameMap = new HashMap<>();
        filenameMap.put("browser_search", "cua_browser_search.png");
        filenameMap.put("search_typed", "cua_search_typed.png");
        filenameMap.put("search_results", "cua_search_results.png");

        Map<String, ScreenshotInfo> screenshots = new HashMap<>();

        for (Map.Entry<String, Path> entry : screenshotPaths.entrySet()) {
            String key = entry.getKey();
            Path path = entry.getValue();

            if (!Files.exists(path)) {
                throw new IOException("Missing required screenshot asset: " + path);
            }

            String imageBase64 = imageToBase64(path);
            String dataUrl = "data:image/png;base64," + imageBase64;
            screenshots.put(key, new ScreenshotInfo(filenameMap.get(key), dataUrl));
        }

        return screenshots;
    }

    /**
     * Find the assets directory relative to the sample.
     *
     * @return Path to the asset directory.
     * @throws IOException If assets directory cannot be found.
     */
    public static Path findAssetsDirectory() throws IOException {
        // Try multiple possible locations
        Path[] possiblePaths = {
            Paths.get("src/samples/resources/assets"),
            Paths.get("src/samples/assets"),
            Paths.get("samples/resources/assets"),
            Paths.get("samples/assets"),
            Paths.get("assets"),
            Paths.get("sdk/ai/azure-ai-agents/src/samples/resources/assets"),
            Paths.get("sdk/ai/azure-ai-agents/src/samples/assets")
        };

        for (Path path : possiblePaths) {
            if (Files.isDirectory(path)) {
                return path;
            }
        }

        // Try using current working directory
        String workDir = System.getProperty("user.dir");
        Path workPath = Paths.get(workDir, "src", "samples", "resources", "assets");
        if (Files.isDirectory(workPath)) {
            return workPath;
        }
        workPath = Paths.get(workDir, "src", "samples", "assets");
        if (Files.isDirectory(workPath)) {
            return workPath;
        }

        throw new IOException("Could not find assets directory. Please create an 'assets' directory with the required screenshot files.");
    }

    /**
     * Process a computer action and simulate its execution.
     *
     * <p>In a real implementation, you might want to execute real browser operations
     * instead of just printing, take screenshots, and return actual screenshot data.</p>
     *
     * @param computerCall The computer tool call from the response.
     * @param currentState Current SearchState of the simulation.
     * @param screenshots Dictionary of screenshot data.
     * @return HandleActionResult containing screenshot info and updated state.
     */
    public static HandleActionResult handleComputerActionAndTakeScreenshot(
            ResponseComputerToolCall computerCall,
            SearchState currentState,
            Map<String, ScreenshotInfo> screenshots) {

        ResponseComputerToolCall.Action action = computerCall.action();
        String actionType = getActionType(action);

        System.out.printf("Executing computer action: %s%n", actionType);

        // State transitions based on actions
        if ("type".equals(actionType)) {
            String text = getTypeText(action);
            if (text != null && !text.isEmpty()) {
                currentState = SearchState.TYPED;
                System.out.printf("  Typing text: '%s' - Simulating keyboard input%n", text);
            }
        } else if ("keypress".equals(actionType)) {
            // Check for ENTER key press
            List<String> keys = getKeyPressKeys(action);
            if (keys.stream().anyMatch(k -> k.contains("Return") || k.contains("ENTER") || k.contains("Enter"))) {
                currentState = SearchState.PRESSED_ENTER;
                System.out.println("  -> Detected ENTER key press");
            }
        } else if ("click".equals(actionType) && currentState == SearchState.TYPED) {
            // Check for click after typing (alternative submit method)
            currentState = SearchState.PRESSED_ENTER;
            System.out.println("  -> Detected click after typing");
        }

        // Provide more realistic feedback based on action type
        printActionFeedback(action, actionType);

        System.out.printf("  -> Action processed: %s%n", actionType);

        // Determine screenshot based on current state
        ScreenshotInfo screenshotInfo;
        if (currentState == SearchState.PRESSED_ENTER) {
            screenshotInfo = screenshots.get("search_results");
        } else if (currentState == SearchState.TYPED) {
            screenshotInfo = screenshots.get("search_typed");
        } else {
            screenshotInfo = screenshots.get("browser_search");
        }

        return new HandleActionResult(screenshotInfo, currentState);
    }

    /**
     * Get the action type from a ResponseComputerToolCall.Action.
     *
     * @param action The action to get the type from.
     * @return The action type as a string.
     */
    public static String getActionType(ResponseComputerToolCall.Action action) {
        if (action.isClick()) {
            return "click";
        } else if (action.isDrag()) {
            return "drag";
        } else if (action.isKeypress()) {
            return "keypress";
        } else if (action.isMove()) {
            return "move";
        } else if (action.isScreenshot()) {
            return "screenshot";
        } else if (action.isScroll()) {
            return "scroll";
        } else if (action.isType()) {
            return "type";
        } else if (action.isWait()) {
            return "wait";
        } else if (action.isDoubleClick()) {
            return "double_click";
        }
        return "unknown";
    }

    /**
     * Get the text from a type action.
     *
     * @param action The action to extract text from.
     * @return The text to type, or null if not a type action.
     */
    public static String getTypeText(ResponseComputerToolCall.Action action) {
        if (action.isType()) {
            return action.asType().text();
        }
        return null;
    }

    /**
     * Get the keys from a keypress action.
     *
     * @param action The action to extract keys from.
     * @return The list of keys, or empty list if not a keypress action.
     */
    public static List<String> getKeyPressKeys(ResponseComputerToolCall.Action action) {
        if (action.isKeypress()) {
            return action.asKeypress().keys();
        }
        return Arrays.asList();
    }

    /**
     * Print feedback based on the action type.
     *
     * @param action The action to print feedback for.
     * @param actionType The type of action.
     */
    public static void printActionFeedback(ResponseComputerToolCall.Action action, String actionType) {
        switch (actionType) {
            case "click":
                ResponseComputerToolCall.Action.Click click = action.asClick();
                System.out.printf("  Click at (%d, %d) - Simulating click on UI element%n", click.x(), click.y());
                break;
            case "double_click":
                ResponseComputerToolCall.Action.DoubleClick doubleClick = action.asDoubleClick();
                System.out.printf("  Double-click at (%d, %d) - Simulating double-click on UI element%n",
                    doubleClick.x(), doubleClick.y());
                break;
            case "drag":
                ResponseComputerToolCall.Action.Drag drag = action.asDrag();
                String pathStr = drag.path().stream()
                    .map(p -> String.format("(%d, %d)", p.x(), p.y()))
                    .collect(Collectors.joining(" -> "));
                System.out.printf("  Drag path: %s - Simulating drag operation%n", pathStr);
                break;
            case "scroll":
                ResponseComputerToolCall.Action.Scroll scroll = action.asScroll();
                System.out.printf("  Scroll at (%d, %d) - Simulating scroll action%n", scroll.x(), scroll.y());
                break;
            case "keypress":
                ResponseComputerToolCall.Action.Keypress keypress = action.asKeypress();
                System.out.printf("  Key press: %s - Simulating key combination%n", keypress.keys());
                break;
            case "screenshot":
                System.out.println("  Taking screenshot - Capturing current screen state");
                break;
            default:
                // No additional feedback for other action types
                break;
        }
    }

    /**
     * Print the final output when the agent completes the task.
     *
     * @param response The response object containing the agent's final output.
     */
    public static void printFinalOutput(Response response) {
        System.out.println("No computer calls found. Agent completed the task:");

        StringBuilder finalOutput = new StringBuilder();
        for (ResponseOutputItem item : response.output()) {
            if (item.isMessage()) {
                for (ResponseOutputMessage.Content part : item.asMessage().content()) {
                    if (part.isOutputText()) {
                        finalOutput.append(part.asOutputText().text()).append("\n");
                    } else if (part.isRefusal()) {
                        finalOutput.append(part.asRefusal().refusal()).append("\n");
                    }
                }
            }
        }

        System.out.printf("Final status: %s%n", response.status().orElse(null));
        System.out.printf("Final result: %s%n", finalOutput.toString().trim());
    }
}
