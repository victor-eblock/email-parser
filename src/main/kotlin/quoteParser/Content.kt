package quoteParser

/**
 * Created by Pavel.Zhuk on 16.08.2016.
 */
data class Content(val body: List<String>,
                   val header: QuoteHeader?,
                   val quote: Content?) {

    fun toString(addMarks: Boolean = false): String {
        val prefix = if (addMarks) "> " else ""
        val separator = if (addMarks) "\n> " else "\n"

        val bodyText = this.body.joinToString(separator = "\n", postfix = "\n")
        val headerText = if (this.header != null && !this.header.text.isEmpty())
            this.header.text.joinToString(prefix = prefix, separator = separator, postfix = "\n")
        else
            ""
        val quoteText = this.quote?.body?.joinToString(prefix = prefix, separator = separator, postfix = "\n") ?: ""

        return StringBuilder(bodyText)
                .append(headerText)
                .append(quoteText)
                .toString()
    }

    override fun toString(): String {
        return toString(addMarks = false)
    }

    companion object {
        fun create(lines: List<String>) = Content(lines, null, null)

        fun create(lines: List<String>, fromIndex: Int, toIndex: Int = fromIndex): Content {
            return Content(
                    lines.subList(0, fromIndex),
                    QuoteHeader(fromIndex, toIndex, lines.subList(fromIndex, toIndex)),
                    Content(
                            lines.subList(toIndex, lines.lastIndex + 1),
                            null,
                            null
                    )
            )
        }
    }
}