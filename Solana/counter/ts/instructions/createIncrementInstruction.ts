import { PublicKey, TransactionInstruction } from '@solana/web3.js';
import { PROGRAM_ID } from '../';

export type IncrementInstructionAccounts = {
    counter: PublicKey,
}
export type IncrementInstructionArgs = {
}


export function createModify0(
    accounts: IncrementInstructionAccounts,
    args: IncrementInstructionArgs
): TransactionInstruction {
    return new TransactionInstruction({
        programId: PROGRAM_ID,
        keys: [
            {
                pubkey: accounts.counter,
                isSigner: false,
                isWritable: true
            }
        ],
        data: Buffer.from([0x0])
    })
}


export function createModify1(
    accounts: IncrementInstructionAccounts,
    args: IncrementInstructionArgs
): TransactionInstruction {
    return new TransactionInstruction({
        programId: PROGRAM_ID,
        keys: [
            {
                pubkey: accounts.counter,
                isSigner: false,
                isWritable: true
            }
        ],
        data: Buffer.from([0x1])
    })
}


export function createModify2(
    accounts: IncrementInstructionAccounts,
    args: IncrementInstructionArgs
): TransactionInstruction {
    return new TransactionInstruction({
        programId: PROGRAM_ID,
        keys: [
            {
                pubkey: accounts.counter,
                isSigner: false,
                isWritable: true
            }
        ],
        data: Buffer.from([0x2])
    })
}


export function createModify3(
    accounts: IncrementInstructionAccounts,
    args: IncrementInstructionArgs
): TransactionInstruction {
    return new TransactionInstruction({
        programId: PROGRAM_ID,
        keys: [
            {
                pubkey: accounts.counter,
                isSigner: false,
                isWritable: true
            }
        ],
        data: Buffer.from([0x3])
    })
}


export function createModify4(
    accounts: IncrementInstructionAccounts,
    args: IncrementInstructionArgs
): TransactionInstruction {
    return new TransactionInstruction({
        programId: PROGRAM_ID,
        keys: [
            {
                pubkey: accounts.counter,
                isSigner: false,
                isWritable: true
            }
        ],
        data: Buffer.from([0x4])
    })
}


export function createModify5(
    accounts: IncrementInstructionAccounts,
    args: IncrementInstructionArgs
): TransactionInstruction {
    return new TransactionInstruction({
        programId: PROGRAM_ID,
        keys: [
            {
                pubkey: accounts.counter,
                isSigner: false,
                isWritable: true
            }
        ],
        data: Buffer.from([0x5])
    })
}


export function createModify6(
    accounts: IncrementInstructionAccounts,
    args: IncrementInstructionArgs
): TransactionInstruction {
    return new TransactionInstruction({
        programId: PROGRAM_ID,
        keys: [
            {
                pubkey: accounts.counter,
                isSigner: false,
                isWritable: true
            }
        ],
        data: Buffer.from([0x6])
    })
}


export function createModify7(
    accounts: IncrementInstructionAccounts,
    args: IncrementInstructionArgs
): TransactionInstruction {
    return new TransactionInstruction({
        programId: PROGRAM_ID,
        keys: [
            {
                pubkey: accounts.counter,
                isSigner: false,
                isWritable: true
            }
        ],
        data: Buffer.from([0x7])
    })
}


export function createModify8(
    accounts: IncrementInstructionAccounts,
    args: IncrementInstructionArgs
): TransactionInstruction {
    return new TransactionInstruction({
        programId: PROGRAM_ID,
        keys: [
            {
                pubkey: accounts.counter,
                isSigner: false,
                isWritable: true
            }
        ],
        data: Buffer.from([0x8])
    })
}


export function createModify9(
    accounts: IncrementInstructionAccounts,
    args: IncrementInstructionArgs
): TransactionInstruction {
    return new TransactionInstruction({
        programId: PROGRAM_ID,
        keys: [
            {
                pubkey: accounts.counter,
                isSigner: false,
                isWritable: true
            }
        ],
        data: Buffer.from([0x9])
    })
}


export function createIncrementInstructionA(
    accounts: IncrementInstructionAccounts,
    args: IncrementInstructionArgs
): TransactionInstruction {
    return new TransactionInstruction({
        programId: PROGRAM_ID,
        keys: [
            {
                pubkey: accounts.counter,
                isSigner: false,
                isWritable: true
            }
        ],
        data: Buffer.from([0x0])
    })
}

export function createIncrementInstructionB(
    accounts: IncrementInstructionAccounts,
    args: IncrementInstructionArgs
): TransactionInstruction {
    return new TransactionInstruction({
        programId: PROGRAM_ID,
        keys: [
            {
                pubkey: accounts.counter,
                isSigner: false,
                isWritable: true
            }
        ],
        data: Buffer.from([0x1])
    })
}

export function createIncrementInstructionC(
    accounts: IncrementInstructionAccounts,
    args: IncrementInstructionArgs
): TransactionInstruction {
    return new TransactionInstruction({
        programId: PROGRAM_ID,
        keys: [
            {
                pubkey: accounts.counter,
                isSigner: false,
                isWritable: true
            }
        ],
        data: Buffer.from([0x2])
    })
}

