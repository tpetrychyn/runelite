/*
 * Copyright (c) 2019, Null (zeruth)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.mixins;

import java.math.BigInteger;
import net.runelite.api.mixins.Copy;
import net.runelite.api.mixins.Mixin;
import net.runelite.api.mixins.Replace;
import net.runelite.api.mixins.Shadow;
import net.runelite.rs.api.RSBuffer;
import net.runelite.rs.api.RSClient;

@Mixin(RSBuffer.class)
public abstract class RSBufferMixin implements RSBuffer
{
	@Shadow("client")
	private static RSClient client;

	@Shadow("modulus")
	private static BigInteger modulus = new BigInteger("c47adeaaeb3fdb519bfd7b1f2cd99fad0be7ab332eddcbc62293c1dcd7cb6081f7262f87143b0a2e295228280c52d38c2a0274abd4d96d8ab9b86b35d258864396c220ff9fdafe75153e9b6fe19866e1a802370476d72e6a03e804b08fe02801edfb8551546221f7e3851fdccc827647ea185e6dc6629b4722a7c47973e5d96a946b0daf5d7ccda67b4db7fbe9fda474adccfc41aa10b2f5e457ab169901a0935872a8b14a1865401e176597d3d30864cdbc66fc46116e772a358227b7e865bac89cdfc1cfce570a908d1b22dd63a085e28dc3fc70bf6564c781f12de08c085a098354d9f03a8d0bc9dc364d0ce2d57ad6d90ea03231b1a9ad067b56e6529d43", 16);

	@Copy("encryptRsa")
	abstract void rs$encryptRsa(BigInteger var1, BigInteger var2);

	@Replace("encryptRsa")
	public void rl$encryptRsa(BigInteger exp, BigInteger mod)
	{
		if (modulus != null)
		{
			mod = modulus;
		}

		rs$encryptRsa(exp, mod);
	}
}