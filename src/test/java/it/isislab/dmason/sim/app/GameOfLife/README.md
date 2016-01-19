<!--
title: Gome Of Life
layout: default
authors: Gennaro Cordasco, Carmine Spagnuolo and Vittorio Scaran
-->

# Game Of Life Model Overview

The Game of Life, also known simply as Life, is a cellular automaton devised by the British mathematician John Horton Conway in 1970.
	

The _game_ is a zero-player game, meaning that its evolution is determined by its initial state, requiring no further input. One interacts with the Game of Life by creating an initial configuration and observing how it evolves or, for advanced players, by creating patterns with particular properties.


## Model Description

The universe of the Game of Life is an infinite two-dimensional orthogonal grid of square cells, each of which is in one of two possible states, alive or dead. Every cell interacts with its eight neighbours, which are the cells that are horizontally, vertically, or diagonally adjacent. At each step in time, the following transitions occur:

- Any live cell with fewer than two live neighbours dies, as if caused by under-population.
- Any live cell with two or three live neighbours lives on to the next generation.
- Any live cell with more than three live neighbours dies, as if by over-population.
- Any dead cell with exactly three live neighbours becomes a live cell, as if by reproduction.

The initial pattern constitutes the seed of the system. The first generation is created by applying the above rules simultaneously to every cell in the seed—births and deaths occur simultaneously, and the discrete moment at which this happens is sometimes called a tick (in other words, each generation is a pure function of the preceding one). The rules continue to be applied repeatedly to create further generations.

####References:
* [https://en.wikipedia.org/wiki/Conway%27s_Game_of_Life](https://en.wikipedia.org/wiki/Conway%27s_Game_of_Life)
* [Gardner, Martin (October 1970). Mathematical Games – The fantastic combinations of John Conway's new solitaire game "life". Scientific American 223.](Gardner, Martin (October 1970). Mathematical Games – The fantastic combinations of John Conway's new solitaire game "life". Scientific American 223.)

### Why this model?

As described above, the game is completely deterministic and embarrassingly parallelizable (the behavior of a cell depends only on the neighboring cells). Behind deterministic this model is extremely helpful as test for parallel or distributed implementations of ABM on a 2D field (it allows to check.that the synchronization among the Logical Processors (LPs)  does not affect the behavior of the model). 

## Initial Conditions

The initial conditions of the GOL model is a matrix of 0 (dead cell) and 1 (alive cell), for instance the Glider 
, shown in following picture

![Glider image](images/glider.gif) 

continuously moves in south-east direction. 


## Model Parameters

| Parameter  | Description   |
|---|---|
| _**W**_  		| the width of the matrix field|
| _**H**_ 		| the height of the matrix field|
| _**S**_ 		| number of discrete steps to be performed|
| _**I**_| initial configuration matrix, the starting configuration is a matrix of 0 and 1, where 0 is dead cell and 1 is alife cell|
| _**F**_| final configuration matrix, the ending configuration is a matrix of 0 and 1, where 0 is dead cell and 1 is alife cell|

##Suggested Benchmark

On a `2000 x 1000` field, we position the Glider (initial conditions) on the top-left (0,0) of the matrix field.
		
		 0, 1, 0 
		 0, 0, 1 
		 1, 1, 1
		 
		 
By construction the glider assumes the same shape each four steps, but moves of one cell in south-east direction. Hence at the end of `7987` time step it reaches the bottom-right of the field.

| Benchmark Name  | Description   | Parameter Values |
|---|---|---|
| Correctness | Benchmark to test the correctness of the model implementation. The correctness is checked verifing  that, starting with the given initial configuration and performing a certain amount of simulation steps the final configuration corresponds with a given final configuration. | _**W**_=`2000`, _**H**_=`1000`, _**S**_= `7987`,  _**I**_= a single glider positioned on the top-left of the field, _**F**_= a single glider matrix postioned on the bottom-right of the field|


## Reference Implementation

A reference implementation is provided as part of the [FLAME GPU SDK](https://github.com/FLAMEGPU/FLAMEGPU/tree/master/examples/CirclesBruteForce_float/src/model).