package InstrumentClasses;

import java.math.BigInteger;

//Adapted from Scorex's Base58 implementation
public class Base58 {

        private static String Alphabet = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
        private static BigInteger Base = BigInteger.valueOf(58);

//        public String encode(byte[] input) {
//                var bi = BigInt(1, input)
//                val s = new StringBuilder()
//        if (bi > 0) {
//            while (bi >= Base) {
//                val mod = bi.mod(Base)
//                s.insert(0, Alphabet.charAt(mod.intValue()))
//                bi = (bi - mod) / Base
//            }
//            s.insert(0, Alphabet.charAt(bi.intValue()))
//        }
//        // Convert leading zeros too.
//        input.takeWhile(_ == 0).foldLeft(s) { case (ss, _) =>
//            ss.insert(0, Alphabet.charAt(0))
//        }.toString()
//  }

        public static byte[] decode(String input) {

            if(input.length() <= 0) {
                throw new IllegalArgumentException("Empty input for Base58.decode");
            }

            BigInteger decoded = decodeToBigInteger(input);

            byte[] bytes = (decoded == BigInteger.valueOf(0))? new byte[0] : decoded.toByteArray();
            // We may have got one more byte than we wanted, if the high bit of the next-to-last byte was not zero.
            // This  is because BigIntegers are represented with twos-compliment notation,
            // thus if the high bit of the last  byte happens to be 1 another 8 zero bits will be added to
            // ensure the number parses as positive. Detect that case here and chop it off.
            boolean stripSignByte = bytes.length > 1 && bytes[0] == 0 && bytes[1] < 0;
            int stripSignBytePos = (stripSignByte)? 1 : 0;
            // Count the leading zeros, if any.
            //val leadingZeros = input.takeWhile(_ == Alphabet.charAt(0)).length

            int leadingZeros = 0;
            while(input.charAt(leadingZeros) == Alphabet.charAt(0)) {
                leadingZeros++;
            }
            // Now cut/pad correctly. Java 6 has a convenience for this, but Android
            // can't use it.
            byte[] tmp = new byte[(bytes.length - stripSignBytePos + leadingZeros)];
            System.arraycopy(bytes, stripSignBytePos, tmp, leadingZeros, tmp.length - leadingZeros);
            return tmp;
        }

        private static BigInteger decodeToBigInteger(String input) {
            BigInteger bigInt = BigInteger.valueOf(0);
            for(int i = input.length() - 1; i >= 0; i--) {
                int alphaIndex = Alphabet.indexOf(input.charAt(i));
                if(alphaIndex == -1) {
                    throw new IllegalArgumentException("Wrong character in Base58 string");
                }
                bigInt = bigInt.add(BigInteger.valueOf(alphaIndex).multiply(Base.pow(input.length() - 1 - i)));
            }
            return bigInt;
        }
}
