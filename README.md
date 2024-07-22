# Chord

Recently emerged blockchain systems have implemented various transaction parallel scheduling mechanisms to improve the system throughput and reduce the latency. However, these mechanisms inevitably introduce bugs. Such bugs can result in severe consequences such as asset loss, double spending, consensus failure, and DDoS. Unfortunately, they have been little analysed about their symptoms and root causes, leading to a lack of effective detection methods. 
In this work, we conduct a thorough analysis of historical transaction parallel scheduling bugs in four commercial blockchains. Results show that most of them arise from mishandling conflict transactions and manifest without obvious phenomena. However, given the heterogeneity of blockchains, it is challenging to trigger conflict handling in a unified way. Effectively identifying these bugs is also hard. Inspired by the findings, we propose Chord, aiming at detecting blockchain transaction parallel scheduling bugs. The key insight of Chord is constructing a unified conflict transaction model to generate conflict resource accesses and proactive reverts. Therefore, Chord triggers the error-prone conflict handling scenarios and effectively triggers the bugs. Besides, Chord incorporates a localremote differential oracle and a TPS oracle to capture the bugs. Our evaluation shows that Chord successfully detected 54 bugs, including 10 previously unknown ones. Chord outperforms the existing methods by decreasing the TPS by 49.7% and increasing the latency by 388.0%, showing its effectiveness in triggering various conflict handling scenarios and exposing the bugs.

# Quickstart

## Chord for FISCO BCOS

### prerequisites

Setup fisco network environment, can be found in https://fisco-bcos-doc.readthedocs.io/zh-cn/latest/docs/quick_start/air_installation.html

### setup FISCO BCOS testnet & start testing
1.Running 4 node private chain 
2.Enable sharding
```
FISCO-BCOS/console/console.sh setSystemConfigByKey feature_sharding 1
```
3.Compile the Java file and run it
```
cd ./FISCO BCOS/java-sdk-demo
./gradlew goJF
bash gradlew build
cd dist
java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.fuzz.Main
```



## Chord for Sei

### prerequisites


Setup Sei network environment, can be found in https://www.docs.sei.io/

### setup Sei testnet & start testing
1.Running 4 node private chain 
2.Compile the Java file and run it

```
cd Sei/untitled
java -jar untitle.jar
```




## Chord for Aptos

### prerequisites

Setup Aptos network environment, can be found in https://aptos.dev/en/build/get-started

### setup Aptos testnet & start testing

1.Running 4 node private chain

2.Run the python script to test
```
cd Aptos/sdk
python3 fuzz.py
```



## Chord for Solana

### prerequisites

Setup Solana network environment, can be found in https://solana.com/docs

### setup Solana testnet & start fuzzing

1.Running 4 node private chain
2.Run the python script to test
```
cd Solana/counter
npm run test
```

