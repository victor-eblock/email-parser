/*
 * Copyright 2016-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package executable

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import jakarta.mail.Folder
import jakarta.mail.Message
import jakarta.mail.Session

private val FILTER_STRING = "##- Please type your reply above this line -##"

private class GmailMailbox(val login: String, val pass: String) {
    fun readInbox() {

        println("Connecting to mailbox...")

        val session = Session.getDefaultInstance(Properties())
        val store = session.getStore("imaps")
        store.connect("imap.gmail.com", login, pass)

        println("Connected.")
        println("Opening 'Inbox' folder...")

        val inbox: Folder = store.getFolder("inbox");
        if (inbox.exists()) {
            inbox.open(Folder.READ_ONLY);

            println("Total messages: " + inbox.messageCount);
            println("Filtering...");

            val filteredMessages = 
                    filterMessages(inbox.messages.asList())

            println("Total messages after filtration: " + filteredMessages.size);

            if (filteredMessages.size == 0) {
                println("Nothing to save to the disk.")
            } else {
                saveMessages(filteredMessages)
                
                println("All messages saved.");
            }            

            inbox.close(false);
        } else {
            println("Can't open 'Inbox' folder.")
        }
        
        println("Done.")
        
        store.close();
    }


    /**
     * Filter messages without appropriate Content Type
     */
    private fun filterMessages(messages: List<Message>): List<Message> =
            messages.filterIndexed { i, message ->
                if (i % 50 == 0)
                    println("$i messages filtered.")

                !message.isMimeType("text/plain") && !message.isMimeType("multipart/alternative")
            }


    private fun saveMessages(messages: List<Message>) {
        val path = ".${File.separator}src${File.separator}main${File.separator}" +
                "resources${File.separator}YT${File.separator}"

        messages.forEachIndexed { i, message ->
            if (i % 50 == 0)
                println("$i messages saved.")

            val fos = FileOutputStream(File("$path$i.eml"))
            message.writeTo(fos)
            fos.close()
        }
    }
}

fun main(args: Array<String>) {
    val properties = Properties()

    try {
        properties.load(FileInputStream(File(
                ".${File.separator}src${File.separator}main${File.separator}" +
                        "resources${File.separator}mail.properties"
        )))

        val gmail = GmailMailbox(
                properties.getProperty("loginTest"),
                properties.getProperty("passwordTest")
        )

        gmail.readInbox()

    } catch(e: IllegalArgumentException) {
        print("Couldn't find properties file: mail.properties.")
        System.exit(-1)
    } catch(e: IllegalStateException) {
        print("One or more necessary properties are missing.")
        System.exit(-1)
    } catch(e: IOException) {
        print("Properties file is incorrect.")
        System.exit(-1)
    }
}
