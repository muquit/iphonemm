#!/usr/bin/perl

# 
# you do not need to know what it does.
# 
# muquit@muquit.com Aug-26-2007  
use strict;

my $bfish_dir="/bfish/mydev/muquit.com/content/iphonemm";
check_dir($bfish_dir);


# remove everything inside the directory
my $cmd="/bin/rm -rf ${bfish_dir}/*";
system("$cmd");

# make sure to generate content of www by compiling.
my $web_dir="www/com.muquit.iPhoneMM";
check_dir($web_dir);

# copy everything 
$cmd="/bin/cp -r ${web_dir}/* ${bfish_dir}";
system("$cmd");

$cmd="/bin/ln ${bfish_dir}/iPhoneMM.html ${bfish_dir}/index.html";
system("$cmd");

$cmd="/bin/ls -lt ${bfish_dir}";
system("$cmd");

print_hints();

##----
# check if a dir exists. if not exit
##----
sub check_dir($)
{
    my $dir=shift;
    if (! -d "$dir")
    {
        print <<EOF;
Directory: '$dir' does not exist
exiting..
EOF
;
        exit;
    }
}

sub print_hints()
{
    print<<EOF;
Hints:
#   * from windows: cd n:/xyz
#   * rm -rf www;./iPhoneMM-compile.cmd
#   * perl cp_to_web.pl
#   * goto bfish
#   * make iphone
#   * make List
#   * make publish

EOF
;
}
