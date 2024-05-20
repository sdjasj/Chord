// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.0;

import "./ContractSharedTwo.sol";
import "./ContractSharedThree.sol";

contract ContractSharedOne {
    uint sharedVal;

    function set(uint x) public {
        sharedVal += x;
    }

    function get() public view returns(uint) {
        return sharedVal;
    }

    function setThree(address one, uint x) public {
        ContractSharedThree(one).set(x);
    }


    function setTwo(address one, uint x) public {
        ContractSharedTwo(one).set(x);
    }

    function getThree(address one) public returns(uint) {
        return ContractSharedThree(one).get();
    }

    function getTwo(address one) public returns(uint) {
        return ContractSharedTwo(one).get();
    }

}
