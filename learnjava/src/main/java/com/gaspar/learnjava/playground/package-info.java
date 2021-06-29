/**
 * The playground package contains classes related to the Playground feature. This
 * feature allows the user to create small Java programs, and run them remotely, with the help
 * of a code running API. The {@link com.gaspar.learnjava.playground.PlaygroundActivity} is displaying the
 * UI that allows for code editing and running.
 * <ul>
 *     <li>{@link com.gaspar.learnjava.playground.InputFragment}: The user can enter input that their program will receive.</li>
 *     <li>{@link com.gaspar.learnjava.playground.CodeFragment}: The user can edit formatted code samples here.</li>
 *     <li>{@link com.gaspar.learnjava.playground.OutputFragment}: The user sees the output of their program here.</li>
 * </ul>
 * The code samples are saved in the database, using {@link com.gaspar.learnjava.database.PlaygroundFile}.
 */
package com.gaspar.learnjava.playground;