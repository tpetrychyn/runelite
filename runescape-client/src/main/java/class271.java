import net.runelite.mapping.ObfuscatedName;
import net.runelite.mapping.ObfuscatedSignature;

@ObfuscatedName("jj")
public class class271 {
	@ObfuscatedName("q")
	@ObfuscatedSignature(
		signature = "Ljj;"
	)
	static final class271 field3589;
	@ObfuscatedName("w")
	@ObfuscatedSignature(
		signature = "Ljj;"
	)
	static final class271 field3582;
	@ObfuscatedName("e")
	@ObfuscatedSignature(
		signature = "Ljj;"
	)
	static final class271 field3583;
	@ObfuscatedName("p")
	@ObfuscatedSignature(
		signature = "Ljj;"
	)
	static final class271 field3591;
	@ObfuscatedName("k")
	@ObfuscatedSignature(
		signature = "Ljj;"
	)
	static final class271 field3585;
	@ObfuscatedName("l")
	@ObfuscatedSignature(
		signature = "Ljj;"
	)
	static final class271 field3581;
	@ObfuscatedName("b")
	@ObfuscatedSignature(
		signature = "Ljj;"
	)
	static final class271 field3584;
	@ObfuscatedName("i")
	@ObfuscatedSignature(
		signature = "Ljj;"
	)
	static final class271 field3588;
	@ObfuscatedName("c")
	@ObfuscatedSignature(
		signature = "Ljj;"
	)
	static final class271 field3600;
	@ObfuscatedName("f")
	@ObfuscatedSignature(
		signature = "Ljj;"
	)
	static final class271 field3593;
	@ObfuscatedName("m")
	@ObfuscatedSignature(
		signature = "Ljj;"
	)
	static final class271 field3590;
	@ObfuscatedName("u")
	@ObfuscatedSignature(
		signature = "Ljj;"
	)
	static final class271 field3592;
	@ObfuscatedName("x")
	@ObfuscatedSignature(
		signature = "Ljj;"
	)
	public static final class271 field3587;
	@ObfuscatedName("r")
	@ObfuscatedSignature(
		signature = "Ljj;"
	)
	public static final class271 field3594;
	@ObfuscatedName("v")
	@ObfuscatedSignature(
		signature = "Ljj;"
	)
	static final class271 field3595;
	@ObfuscatedName("y")
	@ObfuscatedSignature(
		signature = "Ljj;"
	)
	static final class271 field3596;
	@ObfuscatedName("g")
	@ObfuscatedSignature(
		signature = "Ljj;"
	)
	static final class271 field3586;
	@ObfuscatedName("a")
	@ObfuscatedSignature(
		signature = "Ljj;"
	)
	static final class271 field3598;
	@ObfuscatedName("j")
	@ObfuscatedSignature(
		signature = "Ljj;"
	)
	static final class271 field3599;
	@ObfuscatedName("t")
	public final String field3597;

	static {
		field3589 = new class271("8", "8");
		field3582 = new class271("15", "15");
		field3583 = new class271("7", "7");
		field3591 = new class271("10", "10");
		field3585 = new class271("16", "16");
		field3581 = new class271("12", "12");
		field3584 = new class271("3", "3");
		field3588 = new class271("6", "6");
		field3600 = new class271("17", "17");
		field3593 = new class271("5", "5");
		field3590 = new class271("9", "9");
		field3592 = new class271("14", "14");
		field3587 = new class271("18", "18");
		field3594 = new class271("13", "13");
		field3595 = new class271("4", "4");
		field3596 = new class271("1", "1");
		field3586 = new class271("11", "11");
		field3598 = new class271("2", "2");
		field3599 = new class271("19", "19");
	}

	class271(String var1, String var2) {
		this.field3597 = var2;
	}

	@ObfuscatedName("q")
	static double method4991(double var0) {
		return Math.exp(-var0 * var0 / 2.0D) / Math.sqrt(6.283185307179586D);
	}

	@ObfuscatedName("w")
	@ObfuscatedSignature(
		signature = "(DDII)[D",
		garbageValue = "-694088627"
	)
	public static double[] method4992(double var0, double var2, int var4) {
		int var5 = var4 * 2 + 1;
		double[] var6 = new double[var5];
		int var7 = -var4;

		for (int var8 = 0; var7 <= var4; ++var8) {
			double var11 = method4991(((double)var7 - var0) / var2) / var2;
			var6[var8] = var11;
			++var7;
		}

		return var6;
	}
}
