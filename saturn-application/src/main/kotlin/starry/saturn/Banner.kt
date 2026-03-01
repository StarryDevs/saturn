package starry.saturn

import java.io.PrintStream

public interface Banner {
    public fun printBanner(out: PrintStream = System.out)
}

public object EmptyBanner : Banner {
    override fun printBanner(out: PrintStream) {}
}
