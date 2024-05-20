// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.0;

import "./ContractSharedOne.sol";
import "./ContractSharedTwo.sol";

contract ContractSharedThree {
    uint sharedVal;

    function set(uint x) public {
        sharedVal += x;
    }

    function get() public view returns(uint) {
        return sharedVal;
    }

    function setOne(address one, uint x) public {
        ContractSharedOne(one).set(x);
    }

    function setTwo(address one, uint x) public {
        ContractSharedTwo(one).set(x);
    }

    function getOne(address one) public returns(uint) {
        return ContractSharedOne(one).get();
    }

    function getTwo(address one) public returns(uint) {
        return ContractSharedTwo(one).get();
    }


}
