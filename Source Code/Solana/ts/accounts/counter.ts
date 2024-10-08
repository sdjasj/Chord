import BN from 'bn.js';

export type Counter = {
    countA: BN,
    countB: BN,
    countC: BN,
    countD: BN,
    countE: BN,
    countF: BN,
    countG: BN,
    countH: BN,
    countI: BN,
    countJ: BN,
    countK: BN,
    countL: BN,
    countM: BN,
    countN: BN,
    countO: BN,
    countP: BN,
    countQ: BN,
    countR: BN,
    countS: BN,
    countT: BN,
    countU: BN,
    countV: BN,
    countW: BN,
    countX: BN,
    countY: BN,      
    countZ: BN,   
}

export const COUNTER_ACCOUNT_SIZE = 8 * 3;

export function deserializeCounterAccount(data: Buffer): Counter {
    if (data.byteLength !== COUNTER_ACCOUNT_SIZE) {
        throw Error(`Need exactly ${COUNTER_ACCOUNT_SIZE} bytes to deserialize counter`);
    }

    const countABytes = data.slice(0, 8);
    const countBBytes = data.slice(8, 16);
    const countCBytes = data.slice(16, 24);
    const countDBytes = data.slice(24, 32);
    const countEBytes = data.slice(32, 40);
    const countFBytes = data.slice(48, 56);
    const countGBytes = data.slice(56, 64);
    const countHBytes = data.slice(64, 72);
    const countIBytes = data.slice(72, 80);
    const countJBytes = data.slice(80, 88);
    const countKBytes = data.slice(88, 96);
    const countLBytes = data.slice(96, 104);
    const countMBytes = data.slice(104, 112);
    const countNBytes = data.slice(112, 120);
    const countOBytes = data.slice(120, 128);
    const countPBytes = data.slice(128, 136);
    const countQBytes = data.slice(136, 144);
    const countRBytes = data.slice(144, 152);
    const countSBytes = data.slice(152, 160);
    const countTBytes = data.slice(160, 168);
    const countUBytes = data.slice(168, 176);
    const countVBytes = data.slice(176, 184);
    const countWBytes = data.slice(184, 192);
    const countXBytes = data.slice(192, 200);
    const countYBytes = data.slice(200, 208);
    const countZBytes = data.slice(208, 216);
;

    const countA = new BN(countABytes, 'le');
    const countB = new BN(countBBytes, 'le');
    const countC = new BN(countCBytes, 'le');
    const countD = new BN(countDBytes, 'le');
    const countE = new BN(countEBytes, 'le');
    const countF = new BN(countFBytes, 'le');
    const countG = new BN(countGBytes, 'le');
    const countH = new BN(countHBytes, 'le');
    const countI = new BN(countIBytes, 'le');
    const countJ = new BN(countJBytes, 'le');
    const countK = new BN(countKBytes, 'le');
    const countL = new BN(countLBytes, 'le');
    const countM = new BN(countMBytes, 'le');
    const countN = new BN(countNBytes, 'le');
    const countO = new BN(countOBytes, 'le');
    const countP = new BN(countPBytes, 'le');
    const countQ = new BN(countQBytes, 'le');
    const countR = new BN(countRBytes, 'le');
    const countS = new BN(countSBytes, 'le');
    const countT = new BN(countTBytes, 'le');
    const countU = new BN(countUBytes, 'le');
    const countV = new BN(countVBytes, 'le');
    const countW = new BN(countWBytes, 'le');
    const countX = new BN(countXBytes, 'le');
    const countY = new BN(countYBytes, 'le');
    const countZ = new BN(countZBytes, 'le');

    return {
        countA,
        countB,
        countC,
        countD,
        countE,
        countF,
        countG,
        countH,
        countI,
        countJ,
        countK,
        countL,
        countM,
        countN,
        countO,
        countP,
        countQ,
        countR,
        countS,
        countT,
        countU,
        countV,
        countW,
        countX,
        countY,
        countZ,
    };
}