package com.microsoft.azure.keyvault.cryptography.test;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.KeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.concurrent.Future;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.azure.keyvault.cryptography.RsaKey;
import com.microsoft.azure.keyvault.cryptography.algorithms.Rsa15;
import com.microsoft.azure.keyvault.cryptography.algorithms.RsaOaep;

public class RsaKeyTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testEncryptDecryptRsa15() throws Exception {

        KeyPair keyPair = getTestKeyMaterial();
        RsaKey key = new RsaKey("foo", keyPair);
        byte[] plaintext = "plaintext".getBytes();

        // Encrypt the plaintext
        Triple<byte[], byte[], String> result = key.encryptAsync(plaintext, null, null, Rsa15.AlgorithmName).get();
        
        byte[] ciphertext = result.getLeft();
        
        assertEquals(Rsa15.AlgorithmName, result.getRight());

        // Decrypt the ciphertext
        Future<byte[]> decryptResult = key.decryptAsync(ciphertext, null, null, null, result.getRight());
        byte[] decrypted = decryptResult.get();

        key.close();

        assertArrayEquals(plaintext, decrypted);
    }

    @Test
    public void testEncryptDecryptRsaOaep() throws Exception {

        KeyPair keyPair = getTestKeyMaterial();
        RsaKey key = new RsaKey("foo", keyPair);
        byte[] plaintext = "plaintext".getBytes();

        // Encrypt the plaintext
        Triple<byte[], byte[], String> result = key.encryptAsync(plaintext, null, null, RsaOaep.AlgorithmName).get();
        
        byte[] ciphertext = result.getLeft();
        
        assertEquals(RsaOaep.AlgorithmName, result.getRight());

        // Decrypt the ciphertext
        Future<byte[]> decryptResult = key.decryptAsync(ciphertext, null, null, null, result.getRight());
        byte[] decrypted = decryptResult.get();

        key.close();

        assertArrayEquals(plaintext, decrypted);
    }

    @Test
    public void testWrapUnwrapRsa15() throws Exception {

        KeyPair keyPair = getTestKeyMaterial();
        RsaKey key = new RsaKey("foo", keyPair);
        byte[] plaintext = "plaintext".getBytes();

        // Encrypt the plaintext
        Pair<byte[], String> result = key.wrapKeyAsync(plaintext, Rsa15.AlgorithmName).get();
        
        byte[] ciphertext = result.getLeft();
        
        assertEquals(Rsa15.AlgorithmName, result.getRight());

        // Decrypt the ciphertext
        Future<byte[]> decryptResult = key.unwrapKeyAsync(ciphertext, result.getRight());
        byte[] decrypted = decryptResult.get();

        key.close();

        assertArrayEquals(plaintext, decrypted);
    }

    @Test
    public void testWrapUnwrapRsaOaep() throws Exception {

        KeyPair keyPair = getTestKeyMaterial();
        RsaKey key = new RsaKey("foo", keyPair);
        byte[] plaintext = "plaintext".getBytes();

        // Encrypt the plaintext
        Pair<byte[], String> result = key.wrapKeyAsync(plaintext, RsaOaep.AlgorithmName).get();
        
        byte[] ciphertext = result.getLeft();
        
        assertEquals(RsaOaep.AlgorithmName, result.getRight());

        // Decrypt the ciphertext
        Future<byte[]> decryptResult = key.unwrapKeyAsync(ciphertext, result.getRight());
        byte[] decrypted = decryptResult.get();

        key.close();

        assertArrayEquals(plaintext, decrypted);
    }

    @Test
    public void testEncryptDecryptDefaultAlgorithm() throws Exception {

        KeyPair keyPair = getTestKeyMaterial();
        RsaKey key = new RsaKey("foo", keyPair);
        byte[] plaintext = "plaintext".getBytes();

        // Encrypt the plaintext
        Triple<byte[], byte[], String> result = key.encryptAsync(plaintext, null, null, null).get();
        
        byte[] ciphertext = result.getLeft();
        
        assertEquals(RsaOaep.AlgorithmName, result.getRight());

        // Decrypt the ciphertext
        Future<byte[]> decryptResult = key.decryptAsync(ciphertext, null, null, null, result.getRight());
        byte[] decrypted = decryptResult.get();

        key.close();

        assertArrayEquals(plaintext, decrypted);
    }

    @Test
    public void testWrapUnwrapDefaultAlgorithm() throws Exception {

        KeyPair keyPair = getTestKeyMaterial();
        RsaKey key = new RsaKey("foo", keyPair);
        byte[] plaintext = "plaintext".getBytes();

        // Encrypt the plaintext
        Pair<byte[], String> result = key.wrapKeyAsync(plaintext, null).get();
        
        byte[] ciphertext = result.getLeft();
        
        assertEquals(RsaOaep.AlgorithmName, result.getRight());

        // Decrypt the ciphertext
        Future<byte[]> decryptResult = key.unwrapKeyAsync(ciphertext, result.getRight());
        byte[] decrypted = decryptResult.get();

        key.close();

        assertArrayEquals(plaintext, decrypted);
    }

    private static KeyPair getTestKeyMaterial() throws Exception {

        return getWellKnownKey();
    }

    private static KeyPair getWellKnownKey() throws Exception {
        BigInteger modulus = new BigInteger("27266783713040163753473734334021230592631652450892850648620119914958066181400432364213298181846462385257448168605902438305568194683691563208578540343969522651422088760509452879461613852042845039552547834002168737350264189810815735922734447830725099163869215360401162450008673869707774119785881115044406101346450911054819448375712432746968301739007624952483347278954755460152795801894283389540036131881712321193750961817346255102052653789197325341350920441746054233522546543768770643593655942246891652634114922277138937273034902434321431672058220631825053788262810480543541597284376261438324665363067125951152574540779");
        BigInteger publicExponent = new BigInteger("65537");
        BigInteger privateExponent = new BigInteger("10466613941269075477152428927796086150095892102279802916937552172064636326433780566497000814207416485739683286961848843255766652023400959086290344987308562817062506476465756840999981989957456897020361717197805192876094362315496459535960304928171129585813477132331538577519084006595335055487028872410579127692209642938724850603554885478763205394868103298473476811627231543504190652483290944218004086457805431824328448422034887148115990501701345535825110962804471270499590234116100216841170344686381902328362376624405803648588830575558058257742073963036264273582756620469659464278207233345784355220317478103481872995809");
        BigInteger primeP = new BigInteger("175002941104568842715096339107566771592009112128184231961529953978142750732317724951747797764638217287618769007295505214923187971350518217670604044004381362495186864051394404165602744235299100790551775147322153206730562450301874236875459336154569893255570576967036237661594595803204808064127845257496057219227");
        BigInteger primeQ = new BigInteger("155807574095269324897144428622185380283967159190626345335083690114147315509962698765044950001909553861571493035240542031420213144237033208612132704562174772894369053916729901982420535940939821673277140180113593951522522222348910536202664252481405241042414183668723338300649954708432681241621374644926879028977");
        BigInteger primeExponentP = new BigInteger("79745606804504995938838168837578376593737280079895233277372027184693457251170125851946171360348440134236338520742068873132216695552312068793428432338173016914968041076503997528137698610601222912385953171485249299873377130717231063522112968474603281996190849604705284061306758152904594168593526874435238915345");
        BigInteger primeExponentQ = new BigInteger("80619964983821018303966686284189517841976445905569830731617605558094658227540855971763115484608005874540349730961777634427740786642996065386667564038755340092176159839025706183161615488856833433976243963682074011475658804676349317075370362785860401437192843468423594688700132964854367053490737073471709030801");
        BigInteger crtCoefficient = new BigInteger("2157818511040667226980891229484210846757728661751992467240662009652654684725325675037512595031058612950802328971801913498711880111052682274056041470625863586779333188842602381844572406517251106159327934511268610438516820278066686225397795046020275055545005189953702783748235257613991379770525910232674719428");

        KeySpec publicKeySpec = new RSAPublicKeySpec(modulus, publicExponent);
        KeySpec privateKeySpec = new RSAPrivateCrtKeySpec(modulus, publicExponent, privateExponent, primeP, primeQ, primeExponentP, primeExponentQ, crtCoefficient);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return new KeyPair(keyFactory.generatePublic(publicKeySpec), keyFactory.generatePrivate(privateKeySpec));
    }

}
