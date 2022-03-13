import tw from "tailwind-styled-components"

export const FooterStyle = tw.footer`
    h-24
    bg-slate-400
`;


export const Footer = (props) => {
    return (
        <FooterStyle>
            footer
        </FooterStyle>
    )
    
}

export default Footer;
