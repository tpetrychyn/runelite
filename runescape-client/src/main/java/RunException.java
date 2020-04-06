import java.applet.Applet;
import net.runelite.mapping.Export;
import net.runelite.mapping.Implements;
import net.runelite.mapping.ObfuscatedGetter;
import net.runelite.mapping.ObfuscatedName;

@ObfuscatedName("ml")
@Implements("RunException")
public class RunException extends RuntimeException {
	@ObfuscatedName("q")
	@Export("RunException_applet")
	public static Applet RunException_applet;
	@ObfuscatedName("e")
	@ObfuscatedGetter(
		intValue = -345454085
	)
	@Export("RunException_revision")
	public static int RunException_revision;
	@ObfuscatedName("p")
	@ObfuscatedGetter(
		intValue = 875770709
	)
	public static int field4066;
	@ObfuscatedName("k")
	@Export("message")
	String message;
	@ObfuscatedName("l")
	@Export("throwable")
	Throwable throwable;

	RunException(Throwable var1, String var2) {
		this.message = var2;
		this.throwable = var1;
	}
}
