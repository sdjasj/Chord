// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.0;

contract ParallelTest1 {
    uint state_a = 0;
    uint state_b = 0;
    uint state_c = 0;
    uint state_d = 0        ;

    function addA(uint x) public {
        state_a += x;
    }

    function addB(uint x) public {
        state_b += x;
    }

    function addC(uint x) public {
        state_c += x;
    }

    function addD(uint x) public {
        state_d += x;
    }

    function addAB(uint x, uint y) public {
        state_a += x;
        state_b += y;
    }

    function A() public view returns(uint) {
        return state_a;
    }

    function B() public view returns(uint) {
        return state_b;
    }

    function C() public view returns(uint) {
        return state_c;
    }

    function D() public view returns(uint) {
        return state_d;
    }
}
