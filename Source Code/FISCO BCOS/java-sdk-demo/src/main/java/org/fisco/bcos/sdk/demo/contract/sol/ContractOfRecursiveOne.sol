// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.0;

import "./ContractOfRecursiveTwo.sol";

contract ContractOfRecursiveOne {
    uint val;

    function recursiveCall(address contractAddress, uint256 count) public {
        if (count == 0) {
            return;
        }
        val += 1;
        ContractOfRecursiveTwo(contractAddress).recursiveCall(address(this), count - 1);
    }
}
