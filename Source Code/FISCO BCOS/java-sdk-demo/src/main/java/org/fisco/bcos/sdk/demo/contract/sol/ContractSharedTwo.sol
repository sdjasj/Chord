// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.0;

import "./ContractSharedOne.sol";
import "./ContractSharedThree.sol";

contract ContractSharedTwo {
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

    function setThree(address one, uint x) public {
        ContractSharedThree(one).set(x);
    }

    function getOne(address one) public view returns(uint) {
        return ContractSharedOne(one).get();
    }

    function getThree(address one) public view returns(uint) {
        return ContractSharedThree(one).get();
    }


}
