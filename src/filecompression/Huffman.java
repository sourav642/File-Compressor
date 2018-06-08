/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filecompression;

import java.io.*;
import java.util.*;
/*
 *
 * @author Sourav
 
 
// node class is the basic structure
// of each node present in the huffman - tree.
*/
class Data
{   char charArray;
    int charfreq;
    int  code_len;
    public Data()
    {
        charArray='\0';
        charfreq=code_len=0;
    }
}
class HuffmanNode {
 
    int data;
    char c;
 
    HuffmanNode left;
    HuffmanNode right;
}

 
//Comparator class helps to compare the nodes.
class MyComparator implements Comparator<HuffmanNode> {
    public int compare(HuffmanNode x, HuffmanNode y)
    {
 
        return x.data - y.data;
    }
}
    
class Huffman
{
    String[] str;
    private static final char  pseudo_EOF=(char)(-1);//Marks the end of file during decompression
    HuffmanNode root;
     static Data[] data;
     HashMap<String,Character> map;
    public Huffman()
    {
        str=new String[257];
        data=new Data[257];
        root=null;
        for(int i=0;i<=256;i++)
            data[i]=new Data();
        map=new HashMap<String,Character>();
    }
    void code_gen(HuffmanNode root, String s)//Recursive function to generate code lengths from regular huffman codes.
    {
 
        if(root==null)
            return;
        // base case; if the left and right are null
        // then its a leaf node.
        if (root.left== null&& root.right== null) 
        {
            // c is the character in the node
            if(root.c==pseudo_EOF)
                data[256].code_len=s.length(); 
            else
                data[(int)root.c].code_len=s.length();
            return;
        }
        //Add 0 when on going left.
        //Add 1 on going right.
        code_gen(root.left, s + "0");
        code_gen(root.right, s + "1");
    }
    
    void display()//Dispaly the canonical Huffman codes. 
    {
        char chr;
        for(int i=0;i<=256;i++)
        {
            chr=data[i].charArray;
            if(chr!='\0' && i!=256)
                System.out.println(chr+":"+str[(int)chr]);
            else if(chr!='\0' && i==256)
                System.out.println("pseudo-EOF"+":"+str[i]);
            
        }          
    }
    void readchar(String file_src) throws IOException//Read each character from the source file.
    {
        int ch;
        FileReader fr=null;
        try
        {
            fr = new FileReader(file_src);
        }
        catch (FileNotFoundException fe)
        {
            fe.printStackTrace();
        }
        while ((ch=fr.read())!=-1)
        {
            if(ch<=255)
            {
                data[ch].charfreq++;
                data[ch].charArray=(char)ch;
            }
        }
        data[256].charArray=pseudo_EOF;//pseudo-EOF
        data[256].charfreq=1;
        fr.close();
    }
    /*
    encode_data() function:
    encodes the encoded String to bit a string and stores it in the destination file
    */
    void encode_data(String file_src,String file_dest) throws IOException
    {       
        int ch,k=0;
        StringBuilder temp=new StringBuilder();
        FileReader fr=null;
        try
        {
            fr = new FileReader(file_src);
        }
        catch (FileNotFoundException fe)
        {
            fe.printStackTrace();
        }
         while ((ch=fr.read())!=-1)
         {
             temp.append(str[ch]);k++;
         }
         temp.append(str[256]);
         fr.close();
         List<Byte> bytes = new ArrayList<Byte>();
         
		while (temp.length() %8 !=0)
				temp.append('0');
                for(int i=8;i<=temp.length();i+=8)
                {
                    String s = temp.substring(i-8, i);
			bytes.add((byte) Integer.parseInt(s, 2));
                }
                byte[] arr = new byte[bytes.size()];
		for (int i = 0; i < bytes.size(); i++)
                {	arr[i] = bytes.get(i);
                        
                }
                try (FileOutputStream fileOuputStream = new FileOutputStream(file_dest)) 
                {
                    fileOuputStream.write(arr);
                }       
                catch (IOException e) 
                {
                    e.printStackTrace();
                }
                System.out.print("\n\n\nOriginal File: "+file_src.substring(file_src.lastIndexOf("\\")+1)+"\n\nCompressed File: "+file_dest.substring(file_dest.lastIndexOf("\\")+1)+"\n\n\n");
                System.out.print("Original file size: "+k+" Bytes"+"\n\n"+"Compressed file size: "+arr.length+" Bytes");
                System.out.print("\n\n\nCompression ratio: "+((double)k/(double)arr.length)+"\n\n");
                System.out.print("\nPath to compressed file: "+file_dest);      
    }
    /*
    decode_data() function:
    decodes the bytes from Ouput file to generate Original source file
    */
    //Still trying to build this function with an efficient header..
    void decode_data(String file_src,String file_dest) throws IOException
    {
        int k=0;
        StringBuilder temp=new StringBuilder();
        File file=new File(file_src);
        byte[] bits=new byte[(int)file.length()];
        DataInputStream in=new DataInputStream(new FileInputStream(file));
        in.readFully(bits);
        in.close();
        for (int i = 0; i < bits.length; i++)
			temp.append(String.format("%8s", Integer.toBinaryString(bits[i] & 0xFF)).replace(' ', '0'));
          
       
        FileWriter fw=new FileWriter(file_dest);
        int l;String decode;
        for(int i=0;i<temp.length();)
        {
            l=data.length-1;
            while(data[l].code_len!=0)
            {
                
                decode=temp.substring(0,data[l].code_len);
                if(map.containsKey(decode))
                {    
                    if(map.get(decode)==pseudo_EOF)
                        return;
                    fw.write(map.get(decode));
                    
                    i+=data[i].code_len;
                    break;
                }
                l--;
            }   
        }
        fw.flush();
        fw.close();
        System.out.print("\n\n\nOriginal File: "+file_src.substring(file_src.lastIndexOf("\\")+1)+"\n\nDecompressed File: "+file_dest.substring(file_dest.lastIndexOf("\\")+1)+"\n\n\n");
        System.out.print("Original file size: "+data.length+" Bytes"+"\n\n"+"Decompressed file size: "+k+" Bytes\n\n");
        System.out.print("\nPath to decompressed file: "+file_dest);
    }
    /*
    buildCanonicalCodes() function:
    generates Cannonical Huffman Codes for the characters.
    */
    void buildCanonicalCodes() throws IOException
    {
        int n=0;//No. of valid characters int the text file
        root=null;
        for(int i=0;i<=256;i++)
        {
            if(data[i].charArray!='\0')
            {  
                n++;
                
            }
        }
       //Min priority queue using min heap.
        PriorityQueue<HuffmanNode> q
            = new PriorityQueue<HuffmanNode>(n, new MyComparator());
 
        for (int i = 0; i <=256; i++) {
 
            // creating a huffman node object
            // and adding it to the priority-queue.
            
            if(data[i].charArray!='\0')
            {
                HuffmanNode hn = new HuffmanNode();
                hn.c = data[i].charArray;
                hn.data =data[i].charfreq;
            
                hn.left = null;
                hn.right = null;
 
                q.add(hn);
            }
        }
        //Exxtrat 2 min vlues sum them up and add to min heap.
        while (q.size() > 1) {
 
            // first min extract.
            HuffmanNode x = q.peek();
            q.poll();
 
            // second min extarct.
            HuffmanNode y = q.peek();
            q.poll();
            // new node f which is equal
            HuffmanNode f = new HuffmanNode();
            // to the sum of the frequency of the two nodes
            // assigning values to the f node.
            f.data = x.data + y.data;
            f.c = '-';
            f.left = x;
            f.right = y;
            root = f;
            q.add(f);
        }
          
        // print the codes by traversing the tree
        code_gen(root, "");
        //Sort the characters based on code lengths of regular huffman codes 
        Arrays.sort(data,new Comparator<Data>() {
            @Override
            public int compare(Data o1, Data o2) {
                return(o1.code_len-o2.code_len);
            }
        });
        //root=null;
        /*
        logic for generating canonical codes:
        assign the character with smallest code length with code=0
        increment the value of code for subsequent characters
        Shift and append zeros if codelength of subsequent character is greater
        */
        int code=0;
       
        for(int i=0;i<256;i++)
        {   
            if(data[i].charArray!='\0')
            {
                
                if(data[i].charArray==pseudo_EOF)
                {
                    str[256]=Integer.toString(code,2);
                     map.put(str[256],pseudo_EOF);
                }
                else
                {
                    str[(int)data[i].charArray]=Integer.toString(code,2);
                    map.put(str[(int)data[i].charArray],data[256].charArray);
                }
                    
            code=(code+1)<<((data[i+1].code_len)-(data[i].code_len));
            
            }
            if(data[256].charArray!='\0')
            {
                
                if(data[256].charArray==pseudo_EOF)
                {
                    str[256]=Integer.toString(code,2);
                    map.put(str[256],pseudo_EOF);
                }
                else
                {
                    str[(int)data[256].charArray]=Integer.toString(code,2);
                    map.put(str[(int)data[256].charArray],data[256].charArray);
                }
            }
        }
    }
}
