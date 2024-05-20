import BN from 'bn.js';

export type Counter = {
    countA: BN,
    countB: BN,
    countC: BN,
}

export const COUNTER_ACCOUNT_SIZE = 8 * 3;

export function deserializeCounterAccount(data: Buffer): Counter {
    if (data.byteLength !== COUNTER_ACCOUNT_SIZE) {
        throw Error(`Need exactly ${COUNTER_ACCOUNT_SIZE} bytes to deserialize counter`);
    }

    const countABytes = data.slice(0, 8);
    const countBBytes = data.slice(8, 16);
    const countCBytes = data.slice(16, 24);

    const countA = new BN(countABytes, 'le');
    const countB = new BN(countBBytes, 'le');
    const countC = new BN(countCBytes, 'le');

    return {
        countA,
        countB,
        countC,
    };
}