package csd.uoc.gr.thesis;

import csd.uoc.gr.thesis.lib.Parser;

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        String parsed_content = "empty";
        Scanner in = new Scanner(System.in);
        String input = in.nextLine();
        Parser master = new Parser(input);
        parsed_content = master.getBoilerPipedString();
        System.out.println(parsed_content);
    }
}