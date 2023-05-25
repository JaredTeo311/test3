package com.votingsystem;
import java.security.KeyPair;
import java.util.Map;
import java.util.TreeMap;

import java.util.concurrent.TimeoutException;

import net.i2p.crypto.eddsa.KeyPairGenerator;
import net.i2p.crypto.eddsa.Utils;
import net.i2p.crypto.eddsa.spec.EdDSAPrivateKeySpec;
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec;
import net.i2p.crypto.eddsa.EdDSAPrivateKey;
import net.i2p.crypto.eddsa.EdDSAPublicKey;

import com.bigchaindb.api.AssetsApi;
import com.bigchaindb.util.KeyPairUtils;
import com.bigchaindb.api.TransactionsApi;
import com.bigchaindb.builders.BigchainDbConfigBuilder;
import com.bigchaindb.builders.BigchainDbTransactionBuilder;
import com.bigchaindb.constants.BigchainDbApi;
import com.bigchaindb.model.Asset;
import com.bigchaindb.constants.Operations;
import com.bigchaindb.model.Output;
import com.bigchaindb.model.FulFill;
import com.bigchaindb.model.GenericCallback;
import com.bigchaindb.model.MetaData;
import com.bigchaindb.model.Transaction;
import com.bigchaindb.util.JsonUtils;

public class Ballots {
        public static void main(String[] args) {
                // Setup BigchainDB configuration
                try {
                BigchainDbConfigBuilder
                                .baseUrl("http://bdb-server:9984")
                                .setup();

                // Generate key pairs
                // prepare your keys
                net.i2p.crypto.eddsa.KeyPairGenerator edDsaKpg = new net.i2p.crypto.eddsa.KeyPairGenerator();
                KeyPair serverKeyPair = edDsaKpg.generateKeyPair();
                KeyPair ballotKeyPair = edDsaKpg.generateKeyPair();
                // KeyPair serverKeyPair = KeyPairUtils.generateKeyPair();

                // Create a digital asset for the server
                // Asset ballotAsset = new Asset();
                // ballotAsset.setData(new TreeMap<String, Object>() {{
                // put("ballot", new TreeMap<String, Object>() {{
                // put("ballotId", "0000");
                // put("ballotChoice", "Koh");
                // }});
                // }});
                Map<String, String> ballotAssetData = new TreeMap<String, String>() {
                        {
                                put("ballotId", "0000");
                                put("ballotChoice", "Koh");
                        }
                };

                // New metadata
                // MetaData metaData = new MetaData();
                // metaData.setMetaData("what", "My first BigchainDB transaction");

                // Prepare CREATE transaction with digital asset and send CREATE transaction to
                // BigchainDB
                Transaction createTransaction = BigchainDbTransactionBuilder
                                .init()
                                .addAssets(ballotAssetData, TreeMap.class)
                                .operation(Operations.CREATE)
                                .buildAndSign((EdDSAPublicKey) serverKeyPair.getPublic(),
                                                (EdDSAPrivateKey) serverKeyPair.getPrivate())
                                .sendTransaction();

                // // Send CREATE transaction to BigchainDB
                // Transaction fulfilledCreationTx = BigchainDbTransactionBuilder
                // .addTransaction(preparedCreationTx)
                // .sendTransaction();

                // Check if transaction sent successfully

                // Prepare TRANSFER transaction
                // Generate a new keypair to TRANSFER the asset to
                KeyPair targetKeypair = edDsaKpg.generateKeyPair();

                // Describe the output you are fulfilling on the previous transaction
                // (authenticate ballot)
                final FulFill spendFrom = new FulFill();
                spendFrom.setTransactionId(createTransaction.getId());
                spendFrom.setOutputIndex("0");

                // Change the metadata if you want
                // MetaData transferMetadata = new MetaData();
                // metaData.setMetaData("what2", "My first BigchainDB transaction");

                // the asset's ID is equal to the ID of the transaction that created it
                String assetId = createTransaction.getId();

                // By default, the 'amount' of a created digital asset == "1". So we spend "1"
                // in our TRANSFER.
                String amount = "1";

                // Use the previous transaction's asset and TRANSFER it
                Transaction transferTransaction = BigchainDbTransactionBuilder
                                .init()
                                // .addMetaData(metaData)

                                // source keypair is used in the input, because the current owner is "spending"
                                // the output to transfer it
                                .addInput(null, spendFrom, (EdDSAPublicKey) serverKeyPair.getPublic())

                                // after this transaction, the target 'owns' the asset, so, the new output
                                // includes the target's public key
                                .addOutput(amount, (EdDSAPublicKey) targetKeypair.getPublic())

                                // reference the asset by ID when doing a transfer
                                .addAssets(assetId, String.class)
                                .operation(Operations.TRANSFER)

                                // the source key signs the transaction to authorize the transfer
                                .buildAndSign((EdDSAPublicKey) serverKeyPair.getPublic(),
                                                (EdDSAPrivateKey) serverKeyPair.getPrivate())
                                .sendTransaction();

                // Handke the transferTransaction response
                if (transferTransaction != null) {
                        // Transaction was successfully submitted to the network
                        System.out.println("Transfer transaction was sent successfully. Transaction ID: "
                                        + transferTransaction.getId());
                } else {
                        // Error occurred while submitting the transaction
                        System.out.println("Error sending transfer transaction.");
                }
                
        }catch (Exception e) {
                        e.printStackTrace();
                }
        }
        
}
