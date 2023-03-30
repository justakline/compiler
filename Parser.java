//  ************** REQUIRES JAVA 17 OR ABOVE! (https://adoptium.net/) ************** //
package compiler;

import compiler.src.*;

import java.util.logging.Logger;

/*
    <PROGRAM> ::= <STMT_LIST> $$   
    <STMT_LIST> ::= <STMT> <STMT_LIST> | <EMPTY> 
    <STMT> ::= <ID> := <EXPR> | read <ID> | write <EXPR> | <WHILE_STMT> | <DO_STMT> | <IF_STMT>    
    <EXPR> ::= <TERM> <TERM_TAIL>  
    <TERM_TAIL> ::= <ADD_OP> <TERM> <TERM_TAIL> | <EMPTY>  
    <TERM> ::= <FACTOR> <FACTOR_TAIL>
    <FACTOR_TAIL> ::= <MULT_OP> <FACTOR> <FACTOR_TAIL> | <EMPTY> 
    <FACTOR> ::= ( <EXPR> ) | <ID> | <NUMBER> 
    <CONDITION> ::= <EXPR> <RELATION> <EXPR> 
    <WHILE_STATEMENT> ::= <WHILE> <CONDITION> <DO> <STMT_LIST> <OD> 
    <DO_STATEMENT> ::= <DO> <STMT_LIST> <UNTIL> <CONDITION>
    <IF_STMT> ::= <IF> <CONDITION> <THEN> <STMT_LIST> <ELSE> <STMT_lIST>  <FI>
*/
/**
 * The Syntax Analyzer.
 * <p>
 * ************** NOTE: REQUIRES JAVA 11 OR ABOVE! ******************
 */
public class Parser {

    // The lexer which will provide the tokens
    private final LexicalAnalyzer lexer;

    // The actual "code generator"
    private final CodeGenerator codeGenerator;

    /**
     * This is the constructor for the Parser class which
     * accepts a LexicalAnalyzer and a CodeGenerator object as parameters.
     *
     * @param lexer         The Lexer Object
     * @param codeGenerator The CodeGenerator Object
     */
    public Parser(LexicalAnalyzer lexer, CodeGenerator codeGenerator) {
        this.lexer = lexer;
        this.codeGenerator = codeGenerator;

        // Change this to automatically prompt to see the Open WebGraphViz dialog or
        // not.
        MAIN.PROMPT_FOR_GRAPHVIZ = true;
    }

    /*
     * Since the "Compiler" portion of the code knows nothing about the start rule,
     * the "analyze" method must invoke the start rule.
     *
     * Begin analyzing...
     */
    void analyze() {
        try {
            // Generate header for our output
            TreeNode startNode = codeGenerator.writeHeader("PARSE TREE");

            // THIS IS OUR START RULE
            this.beginParsing(startNode);

            // generate footer for our output
            codeGenerator.writeFooter();

        } catch (ParseException ex) {
            final String msg = String.format("%s\n", ex.getMessage());
            Logger.getAnonymousLogger().severe(msg);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * This is just an intermediate method to make it easy to change the start rule
     * of the grammar.
     *
     * @param parentNode The parent node for the parse tree
     * @throws ParseException If there is a syntax error
     */
    private void beginParsing(final TreeNode parentNode) throws ParseException {
        // Invoke the start rule.
        // TODO: Change if necessary!
        this.SENTENCE(parentNode);
    }

    // <PROGRAM> ::= <STMT_LIST> $$

    private void PROGRAM(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode);
        this.STMT_LIST(thisNode);
        // Test for the end of input.
        if (lexer.currentToken() != Token.$$) {
            this.raiseException(Token.$$, thisNode);
        }
    }

    // How do I know when to do the recursive call or the empty??
    // <STMT_LIST> ::= <STMT> <STMT_LIST> | <EMPTY>
    private void STMT_LIST(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode);
        this.STMT(thisNode);

        // These are the FIRST(STMT_LIST)
        List<Token> stmtFirstList = new List<>() {
            {
                add(Token.UNKNOWN);
                add(Token.READ);
                add(Token.WRITE);
                add(Token.WHILE);
                add(Token.DO);
                add(Token.IF);
            }
        };
        if (stmtFirstList.contains(lexer.currentToken())) {
            this.MATCH(thisNode, Token.STMT_LIST);
        } else {
            this.EMPTY(thisNode);
        }
    }

    // <STMT> ::= <ID> := <EXPR> | read <ID> | write <EXPR> | <WHILE_STMT> |
    // <DO_STMT> | <IF_STMT>
    private void STMT(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode);
        if (lexer.currentToken() == Token.UNKNOWN) {
            this.MATCH(thisNode, Token.UNKNOWN);
            this.MATCH(thisNode, Token.ASSIGNMENT);
            this.EXPR(thisNode);

        } else if (lexer.currentToken() == Token.READ) {
            this.MATCH(thisNode, Token.READ);
            this.MATCH(thisNode, Token.UNKNOWN);
        } else if (lexer.currentToken() == Token.WRITE) {
            this.MATCH(thisNode, Token.WRITE);
            this.EXPR(thisNode);
        } else if (lexer.currentToken() == Token.WHILE) {
            this.WHILE_STMT(thisNode);
        } else if (lexer.currentToken() == Token.IF) {
            this.IF_STMT(thisNode);
        } else if (lexer.currentToken() == Token.DO) {
            this.DO_STMT(thisNode);
        }
    }

    // <EXPR> ::= <TERM> <TERM_TAIL>
    private void EXPR(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode);
        this.TERM(thisNode);
        this.TERM_TAIL(thisNode);
    }

    // <TERM_TAIL> ::= <ADD_OP> <TERM> <TERM_TAIL> | <EMPTY>
    private void TERM_TAIL(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode);
        this.MATCH(thisNode, Token.ADD_OP);
        this.TERM(thisNode);

        // recursive or empty
        if (lexer.currentToken() == Token.ADD_OP) {
            this.TERM_TAIL(thisNode);
        } else {
            this.EMPTY(thisNode);
        }
    }

    // <TERM> ::= <FACTOR> <FACTOR_TAIL>
    private void TERM(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode);
        this.FACTOR(thisNode);
        this.FACTOR_TAIL(thisNode);
    }

    // <FACTOR_TAIL> ::= <MULT_OP> <FACTOR> <FACTOR_TAIL> | <EMPTY>
    private void FACTOR_TAIL(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode);
        this.MATCH(thisNode, Token.MULT_OP);
        this.FACTOR(thisNode);

        // Recursive or empty
        if (lexer.currentToken() == Token.MULT_OP) {
            this.MATCH(thisNode, Token.FACTOR_TAIL);
        } else {
            this.EMPTY(thisNode);
        }
    }

    // <FACTOR> ::= ( <EXPR> ) | <ID> | <NUMBER>
    private void FACTOR(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode);

        if (lexer.currentToken() == Token.LEFTP) {
            this.MATCH(thisNode, Token.LEFTP);
            this.EXPR(thisNode);
            this.MATCH(thisNode, Token.RIGHTP);
        } else if (lexer.currentToken() == Token.UNKNOWN) {
            this.MATCH(thisNode, Token.UNKNOWN);
        } else {
            this.MATCH(thisNode, Token.NUMBER);
        }

    }

    // <CONDITION> ::= <EXPR> <RELATION> <EXPR>
    private void CONDITION(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode);
        this.EXPR(thisNode);
        this.MATCH(thisNode, Token.RELATION);
        this.EXPR(thisNode);
    }

    // <WHILE_STATEMENT> ::= <WHILE> <CONDITION> <DO> <STMT_LIST <OD>
    private void WHILE_STMT(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode);

        this.MATCH(thisNode, Token.WHILE);
        this.CONDITION(thisNode);
        this.MATCH(thisNode, Token.DO);
        this.STMT_LIST(thisNode);
        this.MATCH(thisNode, Token.OD);
    }

    // <DO_STATEMENT> ::= <DO> <STMT_LIST> <UNTIL> <CONDITION>
    private void DO_STMT(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode);

        this.MATCH(thisNode, Token.DO);
        this.STMT_LIST(thisNode);
        this.MATCH(thisNode, Token.UNTIL);
        this.CONDITION(thisNode);
    }

    // <IF_STMT> ::= <IF> <CONDITION> <THEN> <STMT_LIST> <ELSE> <STMT_lIST> <FI>
    private void IF_STMT(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode);

        this.MATCH(thisNode, Token.IF);
        this.CONDITION(thisNode);
        this.MATCH(thisNode, Token.THEN);
        this.STMT_LIST(thisNode);
        this.MATCH(thisNode, Token.ELSE);
        this.STMT_LIST(thisNode);
        this.MATCH(thisNode, Token.FI);

    }

    /////////////////////////////////////////////////////////////////////////////////////

    /**
     * Add a an EMPTY terminal node (result of an Epsilon Production) to the parse
     * tree.
     * Mainly, this is just done for better visualizing the complete parse tree.
     *
     * @param parentNode The parent of the terminal node.
     */
    void EMPTY(final TreeNode parentNode) {
        codeGenerator.addEmptyToTree(parentNode);
    }

    /**
     * Match the current token with the expected token.
     * If they match, add the token to the parse tree, otherwise throw an exception.
     *
     * @param parentNode    The parent of the terminal node.
     * @param expectedToken The token to be matched.
     * @throws ParseException Thrown if the token does not match the expected token.
     */
    void MATCH(final TreeNode parentNode, final Token expectedToken) throws ParseException {
        final Token currentToken = lexer.currentToken();

        if (currentToken == expectedToken) {
            var currentLexeme = lexer.getCurrentLexeme();
            this.addTerminalToTree(parentNode, currentToken, currentLexeme);
            lexer.advanceToken();
        } else {
            this.raiseException(expectedToken, parentNode);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Add a terminal node to the parse tree.
     *
     * @param parentNode    The parent of the terminal node.
     * @param currentToken  The token to be added.
     * @param currentLexeme The lexeme of the token beign added.
     * @throws ParseException Throws a ParseException if the token cannot be added
     *                        to the tree.
     */
    void addTerminalToTree(final TreeNode parentNode, final Token currentToken, final String currentLexeme)
            throws ParseException {
        var nodeLabel = "<%s>".formatted(currentToken);
        var terminalNode = codeGenerator.addNonTerminalToTree(parentNode, nodeLabel);

        codeGenerator.addTerminalToTree(terminalNode, currentLexeme);
    }

    /**
     * Raise a ParseException if the input cannot be parsed as defined by the
     * grammar.
     *
     * @param expected   The expected token
     * @param parentNode The token's parent node
     */
    private void raiseException(Token expected, TreeNode parentNode) throws ParseException {
        final var template = "SYNTAX ERROR: '%s' was expected but '%s' was found.";
        final var errorMessage = template.formatted(expected.name(), lexer.getCurrentLexeme());
        codeGenerator.syntaxError(errorMessage, parentNode);
    }
}
