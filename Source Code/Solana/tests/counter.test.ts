import {
    Connection,
    Keypair,
    LAMPORTS_PER_SOL,
    Transaction,
    TransactionInstruction,
    sendAndConfirmTransaction,
    SystemProgram,
    TransactionSignature,
    Signer
} from '@solana/web3.js';

import {
    createIncrementInstructionA,
    createIncrementInstructionB,
    createIncrementInstructionC,
    createModify1,
    createModify2,
    createModify3,
    createModify4,
    createModify5,
    createModify6,
    createModify7,
    createModify8,
    createModify9,
    createModify0,
    deserializeCounterAccount,
    Counter,
    COUNTER_ACCOUNT_SIZE,
    PROGRAM_ID,
} from '../ts';

import fs from 'fs';

import * as crypto from 'crypto';
const ACCOUNT_NUM = 31;
const TX_COUNT = 3000;
const INSTRUCTION_COUNT = 1;

class OffChainCounter {
    count_a: bigint;
    count_b: bigint;
    count_c: bigint;
    count_d: bigint;
    count_e: bigint;
    count_f: bigint;
    count_g: bigint;
    count_h: bigint;
    count_i: bigint;
    count_j: bigint;
    count_k: bigint;
    count_l: bigint;
    count_m: bigint;
    count_n: bigint;
    count_o: bigint;
    count_p: bigint;
    count_q: bigint;
    count_r: bigint;
    count_s: bigint;
    count_t: bigint;
    count_u: bigint;
    count_v: bigint;
    count_w: bigint;
    count_x: bigint;
    count_y: bigint;
    count_z: bigint;

    constructor() {
        // Initialize all counters to zero
        this.count_a = BigInt(0);
        this.count_b = BigInt(0);
        this.count_c = BigInt(0);
        this.count_d = BigInt(0);
        this.count_e = BigInt(0);
        this.count_f = BigInt(0);
        this.count_g = BigInt(0);
        this.count_h = BigInt(0);
        this.count_i = BigInt(0);
        this.count_j = BigInt(0);
        this.count_k = BigInt(0);
        this.count_l = BigInt(0);
        this.count_m = BigInt(0);
        this.count_n = BigInt(0);
        this.count_o = BigInt(0);
        this.count_p = BigInt(0);
        this.count_q = BigInt(0);
        this.count_r = BigInt(0);
        this.count_s = BigInt(0);
        this.count_t = BigInt(0);
        this.count_u = BigInt(0);
        this.count_v = BigInt(0);
        this.count_w = BigInt(0);
        this.count_x = BigInt(0);
        this.count_y = BigInt(0);
        this.count_z = BigInt(0);
    }

    applyModify(modifyType: number) {
        const incrementValue = BigInt(10000);
        
        switch (modifyType) {
            case 0:
                this.count_x += incrementValue;
                this.count_r += incrementValue;
                this.count_b += incrementValue;
                this.count_s += incrementValue;
                this.count_c += incrementValue;
                this.count_r += incrementValue;
                this.count_o += incrementValue;
                this.count_h += incrementValue;
                this.count_s += incrementValue;
                this.count_q += incrementValue;
                break;

            case 1:
                this.count_r += incrementValue;
                this.count_u += incrementValue;
                this.count_x += incrementValue;
                this.count_x += incrementValue;
                this.count_q += incrementValue;
                this.count_s += incrementValue;
                this.count_z += incrementValue;
                this.count_s += incrementValue;
                this.count_z += incrementValue;
                this.count_r += incrementValue;
                break;

            case 2:
                this.count_i += incrementValue;
                this.count_j += incrementValue;
                this.count_x += incrementValue;
                this.count_j += incrementValue;
                this.count_j += incrementValue;
                this.count_m += incrementValue;
                this.count_y += incrementValue;
                this.count_q += incrementValue;
                this.count_q += incrementValue;
                this.count_y += incrementValue;
                break;

            case 3:
                this.count_y += incrementValue;
                this.count_a += incrementValue;
                this.count_i += incrementValue;
                this.count_c += incrementValue;
                this.count_l += incrementValue;
                this.count_g += incrementValue;
                this.count_h += incrementValue;
                this.count_s += incrementValue;
                this.count_y += incrementValue;
                this.count_m += incrementValue;
                break;

            case 4:
                this.count_k += incrementValue;
                this.count_i += incrementValue;
                this.count_o += incrementValue;
                this.count_m += incrementValue;
                this.count_f += incrementValue;
                this.count_u += incrementValue;
                this.count_t += incrementValue;
                this.count_r += incrementValue;
                this.count_b += incrementValue;
                this.count_r += incrementValue;
                break;

            case 5:
                this.count_p += incrementValue;
                this.count_j += incrementValue;
                this.count_w += incrementValue;
                this.count_c += incrementValue;
                this.count_q += incrementValue;
                this.count_x += incrementValue;
                this.count_y += incrementValue;
                this.count_c += incrementValue;
                this.count_d += incrementValue;
                this.count_s += incrementValue;
                break;

            case 6:
                this.count_s += incrementValue;
                this.count_f += incrementValue;
                this.count_s += incrementValue;
                this.count_y += incrementValue;
                this.count_d += incrementValue;
                this.count_u += incrementValue;
                this.count_h += incrementValue;
                this.count_a += incrementValue;
                this.count_w += incrementValue;
                this.count_x += incrementValue;
                break;

            case 7:
                this.count_u += incrementValue;
                this.count_z += incrementValue;
                this.count_b += incrementValue;
                this.count_w += incrementValue;
                this.count_t += incrementValue;
                this.count_t += incrementValue;
                this.count_j += incrementValue;
                this.count_o += incrementValue;
                this.count_d += incrementValue;
                this.count_e += incrementValue;
                break;

            case 8:
                this.count_u += incrementValue;
                this.count_k += incrementValue;
                this.count_x += incrementValue;
                this.count_q += incrementValue;
                this.count_x += incrementValue;
                this.count_l += incrementValue;
                this.count_k += incrementValue;
                this.count_v += incrementValue;
                this.count_t += incrementValue;
                this.count_s += incrementValue;
                break;

            case 9:
                this.count_n += incrementValue;
                this.count_m += incrementValue;
                this.count_d += incrementValue;
                this.count_p += incrementValue;
                this.count_o += incrementValue;
                this.count_l += incrementValue;
                this.count_g += incrementValue;
                this.count_n += incrementValue;
                this.count_a += incrementValue;
                this.count_j += incrementValue;
                break;

            default:
                console.error(`Unknown modify type: ${modifyType}`);
        }
    }
}



(async () => {
    function generateRandomUint8Array(length: number): Uint8Array {
        const randomArray = new Uint8Array(length);
        crypto.randomFillSync(randomArray);
        return randomArray;
    }
    
    function getRandomInt(min: number, max: number): number {
        
        return Math.floor(Math.random() * (max - min + 1)) + min;
     }
    
    const connection = new Connection("http://localhost:8899");
    
    let counterKeypairs: Keypair[] = [];
    for (let i = 0; i < ACCOUNT_NUM; i++) {
        counterKeypairs.push(Keypair.fromSeed(generateRandomUint8Array(32)));
    }
    
    console.log("test account generate successfully");
    const SECRET_KEY_BYTES = [29,241,95,27,146,185,87,44,240,178,49,142,222,149,66,234,35,74,47,24,182,110,254,212,147,10,30,153,102,76,175,21,71,124,123,153,183,34,23,136,121,47,81,220,147,222,101,254,187,255,20,40,247,187,46,192,147,153,59,32,236,164,96,134];
    // Randomly generate our wallet
    const payerKeypair = Keypair.fromSecretKey(new Uint8Array(SECRET_KEY_BYTES))
    const payer = payerKeypair.publicKey;
    await connection.requestAirdrop(payer, LAMPORTS_PER_SOL * 10000000000);
    console.log("funding successfully");
    function initData(tx: Transaction, keypair: Signer): () => Promise<TransactionSignature> {
        return () => {
          return sendAndConfirmTransaction(
                    connection,
                    tx,
                    [payerKeypair, keypair],
                    { skipPreflight: true, commitment: 'confirmed' }
                );
        };
    }


    let tx = new Transaction();
    let initFunc: (() => Promise<TransactionSignature>)[] = [];
    for (let i = 0; i < ACCOUNT_NUM; i++) {
        tx = new Transaction();
        tx.add(SystemProgram.createAccount({
            fromPubkey: payer,
            newAccountPubkey: counterKeypairs[i].publicKey,
            lamports: await connection.getMinimumBalanceForRentExemption(COUNTER_ACCOUNT_SIZE),
            space: COUNTER_ACCOUNT_SIZE,
            programId: PROGRAM_ID
        }));

        tx.feePayer = payer;
    
        tx.recentBlockhash = (await connection.getLatestBlockhash('confirmed')).blockhash;

        initFunc.push(initData(tx, counterKeypairs[i]));
    }   
    await Promise.all(initFunc.map(func => func()));

    let offChainCounters: OffChainCounter[] = [];
    for (let i = 0; i < ACCOUNT_NUM; i++) {
        offChainCounters.push(new OffChainCounter());
    }

    console.log("init account successfully!");
    let tx_arr: Transaction[] = [];
    for (let i = 0; i < TX_COUNT; i++) {
        let tx = new Transaction();
        tx.feePayer = payer;
        for (let j = 0; j < INSTRUCTION_COUNT; j++) {
            let t = getRandomInt(0, 2);
            let idx = getRandomInt(0, ACCOUNT_NUM - 1);
            switch (t) {
                // case 0:
                //     tx.add(createIncrementInstructionA({ counter: counterKeypairs[idx].publicKey }, {}));
                //     break;
                // case 1:
                //     tx.add(createIncrementInstructionB({ counter: counterKeypairs[idx].publicKey }, {}));
                //     break;            
                // case 2:
                //     tx.add(createIncrementInstructionC({ counter: counterKeypairs[idx].publicKey }, {}));
                //     break;
                case 0:
                    tx.add(createModify0({ counter: counterKeypairs[idx].publicKey }, {}));
                    offChainCounters[idx].applyModify(0);
                    break;
                case 1:
                    tx.add(createModify1({ counter: counterKeypairs[idx].publicKey }, {}));
                    offChainCounters[idx].applyModify(1);
                    break;
                case 2:
                    tx.add(createModify2({ counter: counterKeypairs[idx].publicKey }, {}));
                    offChainCounters[idx].applyModify(2);
                    break;
                case 3:
                    tx.add(createModify3({ counter: counterKeypairs[idx].publicKey }, {}));
                    offChainCounters[idx].applyModify(3);
                    break;
                case 4:
                    tx.add(createModify4({ counter: counterKeypairs[idx].publicKey }, {}));
                    offChainCounters[idx].applyModify(4);
                    break;
                case 5:
                    tx.add(createModify5({ counter: counterKeypairs[idx].publicKey }, {}));
                    offChainCounters[idx].applyModify(5);
                    break;
                case 6:
                    tx.add(createModify6({ counter: counterKeypairs[idx].publicKey }, {}));
                    offChainCounters[idx].applyModify(6);
                    break;
                case 7:
                    tx.add(createModify7({ counter: counterKeypairs[idx].publicKey }, {}));
                    offChainCounters[idx].applyModify(7);
                    break;
                case 8:
                    tx.add(createModify8({ counter: counterKeypairs[idx].publicKey }, {}));
                    offChainCounters[idx].applyModify(8);
                    break;
                case 9:
                    tx.add(createModify9({ counter: counterKeypairs[idx].publicKey }, {}));
                    offChainCounters[idx].applyModify(9);
                    break;
                default:
                    console.log("error of out scope");
            }
        }
        tx_arr.push(tx);
    }    
    
    
    console.log("init tx successfully!");
    
    function fetchData(index: number): () => Promise<{ endTime: number, elapsedTime: number }> {
        return async () => {
            const startTime = Date.now(); // 获取函数执行前的时间
            try {
                // tx_arr[index].recentBlockhash = (await connection.getLatestBlockhash('confirmed')).blockhash;
                const result = await sendAndConfirmTransaction(
                    connection,
                    tx_arr[index],
                    [payerKeypair],
                    { skipPreflight: true, commitment: 'confirmed' }
                );
                const endTime = Date.now(); // 获取函数执行后的时间
                const elapsedTime = endTime - startTime; // 计算时间差
                // console.log(`函数执行耗时：${elapsedTime} 毫秒`);
                return { endTime, elapsedTime };
                // console.log(result);
            } catch (error) {
                // 在此处处理异常，可以选择打印错误信息或者进行其他操作
                console.error("Error occurred:", error);
                return { endTime: 0, elapsedTime: 0 };
            }
        };
    }
    
    const functionsArray: (() => Promise<{ endTime: number, elapsedTime: number }>)[] = [];
    for (let i = 0; i < TX_COUNT; i++) {
        functionsArray.push(fetchData(i));
    }
    
    const QPSList = [20, 40, 60, 80, 100, 120, 140, 160, 180, 200]
    // const QPSList = [160, 200]
    for (let i = 0; i < 1; i++) {
        for (let j = 0; j < QPSList.length; j++) {
            const qps = QPSList[j];
            let totalElapsedTime = 0;
            let cnt = 0;
            let batchesInProgress = 0; // 记录当前正在进行的批次数
            let isIntervalCompleted = false;
            console.log("QPS: " + qps + " test round " + i + " start");
            const requestPool = [...functionsArray];
            const startTime = Date.now();
            let endTime = Date.now();
            const interval = setInterval(() => {
                if (requestPool.length === 0 && batchesInProgress === 0) {
                    clearInterval(interval); // 清除定时器
                    const endTime = Date.now();
                    const cost = endTime - startTime;
                    const tps = TX_COUNT / (cost / 1000);
                    const latency = totalElapsedTime / TX_COUNT;
                    // console.log("所有请求已发送完成");
                    const content = "QPS: " + qps + " time: " + Date.now() + " tps: " + tps + " latency: " + latency + " cnt:" + cnt + "\n";
                    fs.appendFile("re_noConflict_tps_re.log", content, (err) => {
                        if (err) {
                            console.error('写入文件时出错:', err);
                            return;
                        }
                        console.log('文件写入成功！');
                    });
                    isIntervalCompleted = true;
                    return;
                }
                batchesInProgress++;
                const batch = requestPool.splice(0, qps); // 从总的请求池中取出一批请求
                sendBatchRequests(batch, () => {
                    batchesInProgress--;
                }); // 发送这一批次的请求
            }, 1000);

            async function sendBatchRequests(batch: (() => Promise<any>)[], callback: () => void) {
                const results = await Promise.all(batch.map(func => func())); // 发送批次的请求
                for (let i = 0; i < results.length; i++) {
                    if (results[i].endTime !== 0) {
                        totalElapsedTime += results[i].elapsedTime;
                        // endTime = Math.max(endTime, results[i].endTime)
                        // cnt++; // 访问每个结果的 elapsedTime 属性
                    }
                }
                callback();
                // console.log("当前批次发送完成");
            }

            async function waitForIntervalCompletion() {
                while (!isIntervalCompleted) {
                    await new Promise(resolve => setTimeout(resolve, 1000)); // 每秒检查一次定时器是否完成
                }
                // 定时器完成后，直接 resolve
                return;
            }
            await waitForIntervalCompletion();
            console.log("QPS: " + qps + " test round " + i + " end");
        }
        // const results: TransactionSignature[] = await Promise.all(functionsArray.map(func => func()));
        // Process results as needed
    }
    
    async function fetchAndCompare() {
        let discrepancies: { index: number, onChain: Counter, offChain: OffChainCounter }[] = [];

        for (let i = 0; i < ACCOUNT_NUM; i++) {
            const publicKey = counterKeypairs[i].publicKey;
            const accountInfo = await connection.getAccountInfo(publicKey);
            if (accountInfo === null) {
                console.error(`Account ${publicKey.toBase58()} not found`);
                continue;
            }

            // 反序列化链上数据
            const onChainCounter = deserializeCounterAccount(accountInfo.data);

            // 获取链下数据
            const offChainCounter = offChainCounters[i];

            // 比较链上与链下数据
            if (
                onChainCounter.countA !== offChainCounter.count_a ||
                onChainCounter.countB !== offChainCounter.count_b ||
                onChainCounter.countC !== offChainCounter.count_c ||
                onChainCounter.countD !== offChainCounter.count_d ||
                onChainCounter.countE !== offChainCounter.count_e ||
                onChainCounter.countF !== offChainCounter.count_f ||
                onChainCounter.countG !== offChainCounter.count_g ||
                onChainCounter.countH !== offChainCounter.count_h ||
                onChainCounter.countI !== offChainCounter.count_i ||
                onChainCounter.countJ !== offChainCounter.count_j ||
                onChainCounter.countK !== offChainCounter.count_k ||
                onChainCounter.countL !== offChainCounter.count_l ||
                onChainCounter.countM !== offChainCounter.count_m ||
                onChainCounter.countN !== offChainCounter.count_n ||
                onChainCounter.countO !== offChainCounter.count_o ||
                onChainCounter.countP !== offChainCounter.count_p ||
                onChainCounter.countQ !== offChainCounter.count_q ||
                onChainCounter.countR !== offChainCounter.count_r ||
                onChainCounter.countS !== offChainCounter.count_s ||
                onChainCounter.countT !== offChainCounter.count_t ||
                onChainCounter.countU !== offChainCounter.count_u ||
                onChainCounter.countV !== offChainCounter.count_v ||
                onChainCounter.countW !== offChainCounter.count_w ||
                onChainCounter.countX !== offChainCounter.count_x ||
                onChainCounter.countY !== offChainCounter.count_y ||
                onChainCounter.countZ !== offChainCounter.count_z
                // 如果有更多字段，继续比较
            ) {
                discrepancies.push({
                    index: i,
                    onChain: onChainCounter,
                    offChain: offChainCounter,
                });
            }
        }

        if (discrepancies.length === 0) {
            console.log("链上与链下数据一致！");
        } else {
            console.error("发现链上与链下数据不一致的账户：");
            discrepancies.forEach(d => {
                console.error(`账户索引: ${d.index}, 公钥: ${counterKeypairs[d.index].publicKey.toBase58()}`);
                console.error(`链上数据:`, d.onChain);
                console.error(`链下数据:`, d.offChain);
            });
        }
    }

    await fetchAndCompare();


})();

// let testFunc = async () => {
//     const connection = new Connection("http://localhost:8899");
//     const SECRET_KEY_BYTES = [29,241,95,27,146,185,87,44,240,178,49,142,222,149,66,234,35,74,47,24,182,110,254,212,147,10,30,153,102,76,175,21,71,124,123,153,183,34,23,136,121,47,81,220,147,222,101,254,187,255,20,40,247,187,46,192,147,153,59,32,236,164,96,134];
//     // Randomly generate our wallet
//     const payerKeypair = Keypair.fromSecretKey(new Uint8Array(SECRET_KEY_BYTES))
//     const payer = payerKeypair.publicKey;

//     // Randomly generate the account key 
//     // to sign for setting up the Counter state
//     let seed = Uint8Array.from([
//         70, 60, 102, 100, 70, 60, 102, 100, 70, 60, 102, 100, 70, 60, 102, 100, 70,
//         60, 102, 100, 70, 60, 102, 100, 70, 60, 102, 100, 70, 60, 102, 100,
//       ]);
//     console.log(seed.length)
//     const counterKeypair = Keypair.fromSeed(seed)
//     const counter = counterKeypair.publicKey;

//     // Airdrop our wallet 1 Sol
//     await connection.requestAirdrop(payer, LAMPORTS_PER_SOL * 100);

//     // Create a TransactionInstruction to interact with our counter program
//     const allocIx: TransactionInstruction = SystemProgram.createAccount({
//         fromPubkey: payer,
//         newAccountPubkey: counter,
//         lamports: await connection.getMinimumBalanceForRentExemption(COUNTER_ACCOUNT_SIZE),
//         space: COUNTER_ACCOUNT_SIZE,
//         programId: PROGRAM_ID
//     })
//     const incrementIx: TransactionInstruction = createIncrementInstructionA({ counter }, {});
//     let tx = new Transaction()
//                 .add(incrementIx)
//                 .add(createIncrementInstructionB({ counter }, {}))
//                 .add(createIncrementInstructionC({ counter }, {}))
//                 .add(createIncrementInstructionC({ counter }, {}));

//     // Explicitly set the feePayer to be our wallet (this is set to first signer by default)
//     tx.feePayer = payer;

//     // Fetch a "timestamp" so validators know this is a recent transaction
//     tx.recentBlockhash = (await connection.getLatestBlockhash('confirmed')).blockhash;

//     const counterAccountInfo = await connection.getAccountInfo(counter, { commitment: "confirmed" });
//     // Send transaction to network (local network)
//     await sendAndConfirmTransaction(
//         connection,
//         tx,
//         [payerKeypair],
//         { skipPreflight: true, commitment: 'confirmed' }
//     );

//     // Get the counter account info from network
//     // const counterAccountInfo = await connection.getAccountInfo(counter, { commitment: "confirmed" });

//     if (counterAccountInfo != null) {
//         const counterAccount = deserializeCounterAccount(counterAccountInfo.data);
//         console.log(`[alloc+increment] countA is: ${counterAccount.countA.toNumber()}`);
//         console.log(`[alloc+increment] countB is: ${counterAccount.countB.toNumber()}`);
//         console.log(`[alloc+increment] countC is: ${counterAccount.countC.toNumber()}`);
//     } else {
//         console.log("error of null");
//     }
// };
// testFunc();