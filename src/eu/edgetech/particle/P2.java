package eu.edgetech.particle;

import io.particle.ecjpake.EcJpake;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import static org.junit.Assert.assertArrayEquals;

/**
 *
 * @author frank
 */
public class P2 {

    private final SimDevice device;
    
    public P2(String mobileSecret) {
        
        this.device = new SimDevice(mobileSecret);
    }
            
            
    public static void main(String[] args) {

        try {
            
            String mobileSecret = "passw0rd";

            P2 device = new P2(mobileSecret);
            
            byte[] secret = device.sharedSecret(mobileSecret);

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    public byte[] sharedSecret(String mobileSecret) throws IOException {

        // app
        EcJpake app = new EcJpake(EcJpake.Role.CLIENT, mobileSecret.getBytes());

        // write round 1 to device
        ByteArrayOutputStream cliRound1 = new ByteArrayOutputStream();
        app.writeRound1(cliRound1);
        
        // read round 1 response from device
        app.readRound1( this.device.round1(cliRound1));
        ByteArrayOutputStream cliRound2 = new ByteArrayOutputStream();
        app.writeRound2(cliRound2);

        // read round 2 response from device
        app.readRound2(this.device.round2(cliRound2));

        byte[] sharedSecret = app.deriveSecret();

        assertArrayEquals(sharedSecret, this.device.forAssertionOnlyGetSharedSecret());
        
        return (sharedSecret);
    }
    
    
    private class SimDevice {
        
        private final EcJpake simDevice;
        
        public SimDevice(String mobileSecret) {
            
            this.simDevice = new EcJpake(EcJpake.Role.SERVER, mobileSecret.getBytes());
        }
        
        public ByteArrayInputStream round1(ByteArrayOutputStream appRequest) throws IOException {
            
            simDevice.readRound1(new ByteArrayInputStream(appRequest.toByteArray()));
            ByteArrayOutputStream deviceResponse = new ByteArrayOutputStream();
            simDevice.writeRound1(deviceResponse);
            
            return(new ByteArrayInputStream(deviceResponse.toByteArray()));
        }
        
        public ByteArrayInputStream round2(ByteArrayOutputStream appRequest) throws IOException {
            
            simDevice.readRound2(new ByteArrayInputStream(appRequest.toByteArray()));
            ByteArrayOutputStream deviceResponse = new ByteArrayOutputStream();
            simDevice.writeRound2(deviceResponse);
            
            return(new ByteArrayInputStream(deviceResponse.toByteArray()));
        }
        
        public byte[] forAssertionOnlyGetSharedSecret( ) {
            
            return(simDevice.deriveSecret());
        }
    }
}
