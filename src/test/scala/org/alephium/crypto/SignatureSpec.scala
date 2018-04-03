package org.alephium.crypto

import org.alephium.AlephiumSuite

class SignatureSpec extends AlephiumSuite {
  "ED25519" should "sign correctly" in {
    forAll { (message: Seq[Byte]) =>
      val (sk, pk) = ED25519.generateKeyPair()
      val sign     = ED25519.sign(message, sk)
      ED25519.verify(message, sign, pk) shouldBe true
    }
  }

  it should "be verified with proper public key" in {
    forAll { (message1: Seq[Byte], message2: Seq[Byte]) =>
      whenever(message1 != message2) {
        val (sk1, pk1) = ED25519.generateKeyPair()
        val (_, pk2)   = ED25519.generateKeyPair()
        val signature  = ED25519.sign(message1, sk1)

        ED25519.verify(message1, signature, pk1) shouldBe true
        ED25519.verify(message2, signature, pk1) should not be true
        ED25519.verify(message1, signature, pk2) should not be true
      }
    }
  }
}